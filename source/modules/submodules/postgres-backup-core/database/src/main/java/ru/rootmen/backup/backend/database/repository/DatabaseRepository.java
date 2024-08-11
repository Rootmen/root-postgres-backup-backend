package ru.rootmen.backup.backend.database.repository;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import ru.iedt.database.request.controller.DatabaseController;
import ru.iedt.database.request.controller.entity.BaseEntity;
import ru.rootmen.backup.backend.database.store.DatabaseStore;

@Singleton
public class DatabaseRepository {
  @Inject DatabaseController databaseController;

  public Uni<Boolean> addDatabase(String name, PgPool client) {
    return databaseController
            .runningQuerySetUni(DatabaseStore.storeName, "GET_APP_LIST", "main", 0, new ArrayList<>(), BaseEntity.class, client)
            .replaceWith(true);
  }

}
