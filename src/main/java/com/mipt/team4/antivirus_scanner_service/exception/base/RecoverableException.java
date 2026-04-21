package com.mipt.team4.antivirus_scanner_service.exception.base;

public class RecoverableException extends BaseException {
  public RecoverableException(String message) {
    super(message);
  }

  public RecoverableException(String message, Throwable cause) {
    super(message, cause);
  }
}
