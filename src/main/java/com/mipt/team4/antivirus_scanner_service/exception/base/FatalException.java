package com.mipt.team4.antivirus_scanner_service.exception.base;

public class FatalException extends BaseException {
  public FatalException(String message) {
    super(message);
  }

  public FatalException(String message, Throwable cause) {
    super(message, cause);
  }
}
