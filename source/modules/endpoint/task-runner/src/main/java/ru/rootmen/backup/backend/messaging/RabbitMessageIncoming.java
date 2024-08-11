package ru.rootmen.backup.backend.messaging;

import static com.diogonunes.jcolor.Attribute.*;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import ru.iedt.database.controller.Controller;
import ru.iedt.database.controller.TaskDescription;
import ru.iedt.database.controller.Utils;
import ru.iedt.database.message.format.GenericMessageWriter;
import ru.iedt.database.message.format.MessageUtils;
import ru.iedt.database.messaging.WebsocketMessage;
import ru.iedt.database.messaging.WebsocketRequest;

@ApplicationScoped
public class RabbitMessageIncoming {

  @Inject Controller controller;

  @GrpcClient WebsocketMessage websocket;

  @Incoming("ui-request-incoming")
  public Uni<Void> consumeTask(Message<JsonObject> payload) {
    TaskDescription task = null;
    try {
      task = Utils.parseMessageFromRest(payload);
      Logger.logger(task, "Запуск");
      if (task.isCorrect()) {
        TaskDescription finalTask = task;
        return websocket
            .message(
                WebsocketRequest.newBuilder()
                    .setUser(finalTask.user_id.toString())
                    .setSocket(finalTask.socket)
                    .setTarget("socket")
                    .setTaskName(finalTask.task_name)
                    .setTaskId(finalTask.task_id.toString())
                    .setPayload(MessageUtils.INIT_MESSAGE)
                    .build())
            .onItem()
            .transformToUni(
                u -> controller.runTaskSynchronous(finalTask.task_name, finalTask, websocket))
            .onItem()
            .transformToUni(
                returnTaskType -> {
                  if (returnTaskType.isDeprecated()) {
                    return null;
                  }
                  if (returnTaskType.isSingle()) {
                    return GenericMessageWriter.sendMessagesFromUni(
                            returnTaskType.getUni(),
                            websocket,
                            finalTask,
                            GenericMessageWriter.TARGET_SOCKET)
                        .replaceWithVoid();
                  }
                  return GenericMessageWriter.sendMessagesFromMulti(
                          Uni.createFrom()
                              .item(
                                  () ->
                                      Tuple2.of(
                                          returnTaskType.getSize(), returnTaskType.getMulti())),
                          websocket,
                          finalTask,
                          GenericMessageWriter.TARGET_SOCKET)
                      .replaceWithVoid();
                })
            .onItem()
            .transform(unused -> payload.ack())
            .onItem()
            .transform(
                voidCompletionStage -> {
                  Logger.loggerFinish(finalTask, "Завершение");
                  return null;
                })
            .replaceWithVoid()
            .onFailure()
            .recoverWithUni(
                Unchecked.function(
                    throwable -> {
                      try {
                        throwable.printStackTrace();
                        return websocket
                            .message(
                                WebsocketRequest.newBuilder()
                                    .setUser(finalTask.user_id.toString())
                                    .setSocket(finalTask.socket)
                                    .setTarget("socket")
                                    .setTaskName(finalTask.task_name)
                                    .setTaskId(finalTask.task_id.toString())
                                    .setPayload(MessageUtils.errorMessage(throwable.getMessage()))
                                    .build())
                            .onItem()
                            .transform(helloReply -> payload.ack())
                            .replaceWithVoid();
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }))
            .replaceWithVoid();
      }
    } catch (Exception throwable) {
      throwable.printStackTrace();
      if (task != null)
        return websocket
            .message(
                WebsocketRequest.newBuilder()
                    .setUser(task.user_id.toString())
                    .setSocket(task.socket)
                    .setTarget("socket")
                    .setTaskName(task.task_name)
                    .setTaskId(task.task_id.toString())
                    .setPayload(MessageUtils.errorMessage(throwable.getMessage()))
                    .build())
            .onItem()
            .transform(helloReply -> payload.ack())
            .replaceWithVoid();
    }
    return Uni.createFrom().item(() -> payload.ack()).replaceWithVoid();
  }
}
