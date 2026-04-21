package com.mipt.team4.antivirus_scanner_service.exception.base;

public class BaseException extends RuntimeException {
  public BaseException(String message) {
    super(message);
  }

  public BaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
