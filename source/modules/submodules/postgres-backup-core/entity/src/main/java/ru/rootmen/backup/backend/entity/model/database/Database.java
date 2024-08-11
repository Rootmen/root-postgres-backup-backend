package ru.rootmen.backup.backend.entity.model.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.iedt.database.request.controller.entity.BaseEntity;
import ru.iedt.database.request.controller.entity.annotation.CreateConstructor;

public class Database extends BaseEntity {
  @Schema(title = "Имя базы данных")
  private final String database_name;

  @Schema(title = "UUID базы данных")
  private final UUID database_uuid;

  @Schema(title = "UUID группы баз данных")
  private final UUID database_group_uuid;

  @Schema(title = "Дата проведения последнего бэкапа")
  private final LocalDateTime database_last_backup;

  @JsonCreator
  @CreateConstructor
  public Database(
      @JsonProperty("database_name") String database_name,
      @JsonProperty("database_uuid") UUID database_uuid,
      @JsonProperty("database_group_uuid") UUID database_group_uuid,
      @JsonProperty("database_last_backup") LocalDateTime database_last_backup) {
    this.database_name = database_name;
    this.database_uuid = database_uuid;
    this.database_group_uuid = database_group_uuid;
    this.database_last_backup = database_last_backup;
  }

  @JsonIgnore
  public String getDatabaseName() {
    return database_name;
  }

  @JsonIgnore
  public UUID getDatabaseUuid() {
    return database_uuid;
  }

  @JsonIgnore
  public UUID getDatabaseGroupUuid() {
    return database_group_uuid;
  }

  @JsonIgnore
  public LocalDateTime getDatabaseLastBackup() {
    return database_last_backup;
  }
}
