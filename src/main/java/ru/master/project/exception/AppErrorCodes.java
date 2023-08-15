package ru.master.project.exception;

/**
 * Sevices errors
 */
public enum AppErrorCodes {
  TEMPLATE_ERRORS(1, "Ошибка");

  private final Integer errorCode;
  private final String errorDescription;

  AppErrorCodes(Integer errorCode, String errorDescription) {
    this.errorCode = errorCode;
    this.errorDescription = errorDescription;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public String getErrorDescription() {
    return errorDescription;
  }
}