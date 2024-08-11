package ru.rootmen.backup.backend.rest.bans;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import ru.rootmen.backup.backend.entity.tasks.DatabaseService;
import ru.rootmen.backup.backend.rest.BaseRestController;

@Path("/database")
public class DatabaseListController extends BaseRestController {

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(
            responseCode = "200",
            content = {
                    @Content(
                            mediaType = MediaType.TEXT_PLAIN,
                            schema =
                            @Schema(
                                    implementation = Object.class,
                                    description = DatabaseService.TaskNames.GET_DATABASE_LIST))
            })
    @Operation(summary = "Получение списка баз")
    public Uni<String> getDatabaseList() {
        return this.addRabbitTask(DatabaseService.TaskNames.GET_DATABASE_LIST, "");
    }
}
