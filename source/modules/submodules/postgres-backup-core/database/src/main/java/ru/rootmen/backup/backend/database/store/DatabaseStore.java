package ru.rootmen.backup.backend.database.store;

import io.quarkus.runtime.annotations.RegisterForReflection;
import ru.iedt.database.request.store.DefinitionStore;
import ru.iedt.database.request.store.QueryStoreDefinition;

@RegisterForReflection
@DefinitionStore
public class DatabaseStore extends QueryStoreDefinition {
  public static String storeName = "DATABASE";
  @Override
  public String getResourcePatch() {
    return "/query/DATABASE.xml";
  }

  @Override
  public String getStoreName() {
    return storeName;
  }

  @Override
  public Class<?> getResourceClass() {
    return this.getClass();
  }
}
