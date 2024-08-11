package ru.rootmen.backup.backend.rest;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;
import ru.iedt.database.controller.TaskDescription;

@Path("")
public class SystemController extends BaseRestController {

  @GET
  @Path("/start/task/{TASK_SECRET_NAME}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Operation(
      summary = "Подтверждение запуска задачи",
      description = "Запускает задачу на выполнение")
  public Uni<Response> startTask(@PathParam("TASK_SECRET_NAME") String task_secret_name) {
    if (taskDescriptionHashMap.containsKey(task_secret_name)) {
      TaskDescription taskDescription = taskDescriptionHashMap.get(task_secret_name);
      if (!taskDescription.app_id.equals(appUuid)
          || !taskDescription.user_id.equals(user)
          || !taskDescription.token.equals(token)) {
        return Uni.createFrom()
            .item(
                () ->
                    RestResponse.ResponseBuilder.ok(
                            "Invalid confirm data", MediaType.TEXT_PLAIN_TYPE)
                        .status(Response.Status.BAD_REQUEST)
                        .build()
                        .toResponse());
      }
      this.startRabbitTask(
          taskDescription.task_name, taskDescription.task_data, taskDescription.task_id.toString());
      return Uni.createFrom()
          .item(
              () ->
                  RestResponse.ResponseBuilder.ok("Task Start", MediaType.TEXT_PLAIN_TYPE)
                      .status(Response.Status.OK)
                      .build()
                      .toResponse());
    } else {
      return Uni.createFrom()
          .item(
              () ->
                  RestResponse.ResponseBuilder.ok(
                          "Secret task id not found", MediaType.TEXT_PLAIN_TYPE)
                      .status(Response.Status.BAD_REQUEST)
                      .build()
                      .toResponse());
    }
  }
}
