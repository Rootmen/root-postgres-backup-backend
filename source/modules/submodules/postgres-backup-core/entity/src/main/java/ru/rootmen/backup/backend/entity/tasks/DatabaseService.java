package ru.rootmen.backup.backend.entity.tasks;

public interface DatabaseService {
  interface TaskNames {
    String ADD_DATABASE = "add_database";
    String UPDATE_DATABASE = "update_database";
    String GET_DATABASE = "get_database";
    String GET_DATABASE_LIST = "get_database_list";
    String DELETE_DATABASE = "delete_database";
  }
}
