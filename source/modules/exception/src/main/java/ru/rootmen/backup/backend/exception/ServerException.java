package ru.rootmen.backup.backend.exception;

public class ServerException extends RuntimeException {

  String error;
  String message;
  Exception base;

  public ServerException(String error, String message, Exception base) {
    this.error = error;
    this.message = message;
    this.base = base;
  }

  public ServerException(String message, String error) {
    this.error = error;
    this.message = message;
  }

  public String getError() {
    return error;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Exception getBase() {
    return base;
  }
}
