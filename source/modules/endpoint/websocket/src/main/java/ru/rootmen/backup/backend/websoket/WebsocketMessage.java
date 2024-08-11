package ru.rootmen.backup.backend.websoket;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import java.util.Objects;
import java.util.UUID;
import ru.iedt.database.messaging.WebsocketReply;
import ru.iedt.database.messaging.WebsocketRequest;

@GrpcService
public class WebsocketMessage implements ru.iedt.database.messaging.WebsocketMessage {

  @Override
  public Uni<WebsocketReply> message(WebsocketRequest task) {
    try {
      Uni<Void> uni =
          (Objects.equals(task.getTarget(), "all"))
              ? MainWebSocket.sendMessageBroadcast(
                  getFormatMessage(
                      task.getTaskId(), task.getUser(), task.getTaskName(), task.getPayload()))
              : MainWebSocket.sendMessage(
                  UUID.fromString(task.getUser()),
                  task.getSocket(),
                  getFormatMessage(
                      task.getTaskId(), task.getUser(), task.getTaskName(), task.getPayload()));
      return uni.onFailure()
          .invoke(Throwable::printStackTrace)
          .onItem()
          .transform(unused -> WebsocketReply.newBuilder().setMessage(true).build());
    } catch (Exception e) {
      e.printStackTrace();
      if (task != null) {
        return MainWebSocket.sendMessage(
                UUID.fromString(task.getUser()),
                task.getSocket(),
                String.format(
                    "{ \"error\": true, \"error_message\": \"%s\"}", e.getLocalizedMessage()))
            .onItem()
            .transform(unused -> WebsocketReply.newBuilder().setMessage(false).build());
      }
      return Uni.createFrom().item(() -> WebsocketReply.newBuilder().setMessage(false).build());
    }
  }

  public static String getFormatMessage(String taskId, String userId, String taskName, String data)
      throws Exception {
    return String.format(
        "{ \"task_id\": \"%s\", \"user_id\": \"%s\", \"task_name\": \"%s\", \"data\": %s }",
        taskId, userId, taskName, data);
  }
}
