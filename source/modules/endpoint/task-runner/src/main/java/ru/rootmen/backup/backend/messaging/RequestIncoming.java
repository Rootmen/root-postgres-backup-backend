package ru.rootmen.backup.backend.messaging;

import static ru.iedt.database.message.format.MessageUtils.mapper;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import java.util.UUID;
import ru.iedt.database.controller.Controller;
import ru.iedt.database.controller.TaskDescription;
import ru.iedt.database.message.format.MessageUtils;
import ru.iedt.database.messaging.*;

@GrpcService
public class RequestIncoming implements ru.iedt.database.messaging.RequestIncoming {
  @Inject Controller controller;
  @GrpcClient WebsocketMessage websocket;

  @Override
  public Multi<RequestReply> message(Request task) {

    TaskDescription taskDescription =
        new TaskDescription(
            UUID.fromString(task.getUser()),
            task.getSocket(),
            task.getToken(),
            UUID.fromString(task.getAppUuid()),
            UUID.fromString(task.getTaskUuid()),
            task.getTaskName(),
            task.getPayload());
    Logger.logger(taskDescription, "Запуск");
    return controller
        .runTaskSynchronous(taskDescription.task_name, taskDescription, websocket)
        .onItem()
        .transformToMulti(
            returnTaskType -> {
              if (returnTaskType.isSingle()) {
                return returnTaskType.getUni().toMulti();
              } else {
                return returnTaskType.getMulti();
              }
            })
        .onItem()
        .transform(
            Unchecked.function(
                object ->
                    RequestReply.newBuilder().setResult(mapper.writeValueAsString(object)).build()))
        .onFailure()
        .recoverWithMulti(
            Unchecked.function(
                throwable -> {
                  throwable.printStackTrace();
                  return Multi.createFrom()
                      .item(
                          RequestReply.newBuilder()
                              .setResult(
                                  mapper.writeValueAsString(
                                      MessageUtils.errorMessage(throwable.getMessage())))
                              .build());
                }));
  }
}
