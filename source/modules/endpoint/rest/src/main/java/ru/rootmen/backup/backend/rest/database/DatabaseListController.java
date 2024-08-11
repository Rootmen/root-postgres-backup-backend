package ru.rootmen.backup.backend.rest.database;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import ru.rootmen.backup.backend.entity.model.database.Database;
import ru.rootmen.backup.backend.entity.tasks.DatabaseService;
import ru.rootmen.backup.backend.rest.BaseRestController;

@Path("/database")
public class DatabaseListController extends BaseRestController {

  @GET
  @Path("/list")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Получение списка баз")
  public Uni<UUID> getDatabaseListUUID() {
    return this.addRabbitTask(DatabaseService.TaskNames.GET_DATABASE_LIST, "");
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Получение списка баз")
  public Multi<Database> getDatabaseListJson() {
    return this.startGrpcTask(DatabaseService.TaskNames.GET_DATABASE_LIST, "", Database.class);
  }
}
