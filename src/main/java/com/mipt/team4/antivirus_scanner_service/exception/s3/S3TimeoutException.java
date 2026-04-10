package com.mipt.team4.antivirus_scanner_service.exception.s3;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;

public class S3TimeoutException extends RecoverableException {

  public S3TimeoutException(String s3Key, Throwable cause) {
    super("S3 read timed out for key: " + s3Key, cause);
  }
}
