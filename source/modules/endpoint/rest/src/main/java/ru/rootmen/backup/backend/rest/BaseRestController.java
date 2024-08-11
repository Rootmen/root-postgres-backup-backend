package ru.rootmen.backup.backend.rest;

import static ru.rootmen.backup.backend.entity.ObjectMapper.mapper;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import ru.iedt.database.controller.TaskDescription;
import ru.iedt.database.controller.Utils;
import ru.iedt.database.messaging.*;

@ApplicationScoped
public class BaseRestController {

  @RestHeader("X-Account-id")
  @DefaultValue("9b83c8a9-1284-447a-a14f-70cd362e2ded")
  protected UUID user;

  @RestHeader("X-Socket-id")
  @DefaultValue("12")
  protected String socket = "12";

  @RestHeader("X-Token")
  @DefaultValue("12")
  protected String token = "12";

  @RestHeader("X-Fingerprint")
  @DefaultValue("12")
  protected String fingerprint = "12";

  @RestHeader("X-Task-Start-Urgently")
  @DefaultValue("true")
  protected Boolean taskStartUrgently = true;

  @RestHeader("X-Task-UUID")
  protected UUID taskUUID;

  @Inject
  @Channel("ui-request-outgoing")
  Emitter<String> emitter;

  @GrpcClient("request-incoming")
  RequestIncoming requestIncoming;

  @ServerExceptionMapper
  public RestResponse<String> mapException(RuntimeException x) {
    x.printStackTrace();
    return RestResponse.status(
        Response.Status.BAD_REQUEST,
        String.format("{ \"error\": true, \"error_message\": \"%s\"}", x.getMessage()));
  }

  @ServerRequestFilter
  public Optional<Response> getFilter(ContainerRequestContext ctx) {
    String fingerprint = ctx.getHeaderString("X-Fingerprint");
    if (fingerprint == null || fingerprint.trim().isEmpty()) {
      return Optional.ofNullable(
          RestResponse.ResponseBuilder.ok("No fingerprint in request", MediaType.TEXT_PLAIN_TYPE)
              .status(Response.Status.BAD_REQUEST)
              .build()
              .toResponse());
    }
    return Optional.empty();
  }

  static HashMap<String, TaskDescription> taskDescriptionHashMap = new HashMap<>();

  public Uni<UUID> getTaskUUId(String taskName, String json) {
    return Uni.createFrom().item(() -> Objects.requireNonNullElseGet(taskUUID, UUID::randomUUID));
  }

  public Uni<UUID> addRabbitTask(String taskName, String json) {
    Uni<UUID> uuidUni = getTaskUUId(taskName, json);
    if (taskStartUrgently || taskUUID != null) {
      return uuidUni
          .onItem()
          .transform(
              uuid -> {
                Utils.addRabbitTaskRest(
                    emitter,
                    user.toString(),
                    socket,
                    taskName,
                    token,
                    appUuid.toString(),
                    uuid.toString(),
                    json);
                return uuid;
              });
    }
    return uuidUni
        .onItem()
        .transform(
            uuid -> {
              String hashUuid = String.valueOf(uuid);
              taskDescriptionHashMap.put(
                  hashUuid,
                  new TaskDescription(user, socket, token, appUuid, uuid, taskName, json));
              Uni.createFrom()
                  .voidItem()
                  .onItem()
                  .delayIt()
                  .by(Duration.ofSeconds(2))
                  .map(unused -> taskDescriptionHashMap.remove(hashUuid))
                  .subscribe()
                  .with(taskDescription -> {});
              return uuid;
            });
  }

  public <T> Multi<T> startGrpcTask(String taskName, String json, Class<T> tClass) {
    Uni<UUID> task_uuid = getTaskUUId(taskName, json);
    Multi<RequestReply> result =
        task_uuid
            .onItem()
            .transformToMulti(
                uuid ->
                    requestIncoming.message(
                        Request.newBuilder()
                            .setUser(user.toString())
                            .setSocket(socket)
                            .setToken(token)
                            .setTaskName(taskName)
                            .setTaskUuid(String.valueOf(uuid))
                            .setAppUuid(appUuid.toString())
                            .setPayload(json)
                            .build()));

    if (tClass == Void.class) {
      return (Multi<T>) result.toUni().replaceWithVoid();
    } else {
      return result
          .onItem()
          .transform(
              Unchecked.function(
                  requestReply -> mapper.readValue(requestReply.getResult(), tClass)));
    }
  }

  public void startRabbitTask(String taskName, String json, String task_uuid) {
    Utils.addRabbitTaskRest(
        emitter, user.toString(), socket, taskName, token, appUuid.toString(), task_uuid, json);
  }

  static UUID appUuid = UUID.fromString("91fa9c8e-9aa3-42ab-9192-1276d2d84446");
}
