package ru.rootmen.backup.backend.messaging;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

import com.diogonunes.jcolor.AnsiFormat;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import ru.iedt.database.controller.Controller;
import ru.iedt.database.controller.TaskDescription;
import ru.iedt.database.controller.Utils;
import ru.iedt.database.message.format.MessageUtils;
import ru.iedt.database.messaging.WebsocketMessage;
import ru.iedt.database.messaging.WebsocketRequest;

@ApplicationScoped
public class RabbitMessageIncoming {

  @Inject Controller controller;

  @GrpcClient WebsocketMessage websocket;

  static int isPrint = 31;
  static AnsiFormat tableHead = new AnsiFormat(GREEN_TEXT(), NONE(), NONE());
  static AnsiFormat tableStartRow = new AnsiFormat(BLUE_TEXT(), NONE(), NONE());
  static AnsiFormat tableFinishRow = new AnsiFormat(CYAN_TEXT(), NONE(), NONE());
  static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss SSS");

  @Incoming("ui-request-incoming")
  public Uni<Void> consumeTask(Message<JsonObject> payload) {
    TaskDescription task = null;
    // TODO КОГДА-ТО ЗДЕСЬ БУДЕТ НОРМАЛЬНЫЙ ЛОГГЕР
    // нормальный значит с зелеными и красными логами!
    isPrint++;
    if (isPrint > 30) {
      System.out.printf(
          "%-50s| %-25s| %-50s| %-40s| %-50s| %-50s| %-200s\n",
          colorize("Время", tableHead),
          colorize("Тип задачи", tableHead),
          colorize("UUID задачи", tableHead),
          colorize("Название задачи", tableHead),
          colorize("UUID пользователя", tableHead),
          colorize("Сокет подключения", tableHead),
          colorize("Данные", tableHead));
      isPrint = 0;
    }
    try {
      task = Utils.parseMessageFromRest(payload);
      System.out.printf(
          "%-50s| %-25s| %-50s| %-40s| %-50s| %-50s| %-200s\n",
          colorize(dateFormat.format(new Date()), tableStartRow),
          colorize("Запуск", tableStartRow),
          colorize(task.task_id.toString(), tableStartRow),
          colorize(
              task.task_name.length() > 30
                  ? task.task_name.substring(0, 27) + "..."
                  : task.task_name,
              tableStartRow),
          colorize(task.user_id.toString(), tableStartRow),
          colorize(
              task.socket.length() > 38 ? task.socket.substring(0, 38) + "..." : task.socket,
              tableStartRow),
          colorize(
              task.task_data.length() > 199
                  ? task.task_data.substring(0, 190) + "..."
                  : task.task_data,
              tableStartRow));
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
            .transformToUni(u -> controller.runTask(finalTask.task_name, finalTask, websocket))
            .onItem()
            .transform(unused -> payload.ack())
            .onItem()
            .transform(
                voidCompletionStage -> {
                  System.out.printf(
                      "%-50s| %-25s| %-50s| %-40s\n",
                      colorize(dateFormat.format(new Date()), tableFinishRow),
                      colorize("Завершение", tableFinishRow),
                      colorize(finalTask.task_id.toString(), tableFinishRow),
                      colorize(
                          finalTask.task_name.length() > 30
                              ? finalTask.task_name.substring(0, 27) + "..."
                              : finalTask.task_name,
                          tableFinishRow));
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
