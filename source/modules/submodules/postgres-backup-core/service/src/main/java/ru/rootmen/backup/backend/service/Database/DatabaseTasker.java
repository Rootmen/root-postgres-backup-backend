package ru.rootmen.backup.backend.service.Database;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ru.iedt.database.controller.TaskDescription;
import ru.iedt.database.controller.annotation.TaskSynchronous;
import ru.iedt.database.controller.annotation.Tasks;
import ru.iedt.database.messaging.WebsocketMessage;
import ru.rootmen.backup.backend.database.repository.DatabaseRepository;
import ru.rootmen.backup.backend.entity.model.database.Database;
import ru.rootmen.backup.backend.entity.tasks.DatabaseService;

@Tasks
@Unremovable
@RegisterForReflection
@Singleton
public class DatabaseTasker {
  @Inject PgPool client;

  @Inject DatabaseRepository databaseRepository;

  @TaskSynchronous(DatabaseService.TaskNames.GET_DATABASE_LIST)
  public Uni<Tuple2<Integer, Multi<Database>>> addDatabase(
      TaskDescription task, WebsocketMessage websocketMessage) {
    return Uni.createFrom()
        .voidItem()
        .onItem()
        .transformToUni(Unchecked.function(ban -> databaseRepository.getDatabaseList(client)));
  }
}
