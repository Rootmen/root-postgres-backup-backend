package ru.rootmen.backup.backend.database.repository;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import ru.iedt.database.request.controller.DatabaseController;
import ru.rootmen.backup.backend.database.store.DatabaseStore;
import ru.rootmen.backup.backend.entity.model.database.Database;

@Singleton
public class DatabaseRepository {
  @Inject DatabaseController databaseController;

  public Uni<Tuple2<Integer, Multi<Database>>> getDatabaseList(PgPool client) {
    return databaseController.runningQuerySetMulti(
        DatabaseStore.storeName, "GET_DATABASE", new ArrayList<>(), Database.class, client);
  }
}
