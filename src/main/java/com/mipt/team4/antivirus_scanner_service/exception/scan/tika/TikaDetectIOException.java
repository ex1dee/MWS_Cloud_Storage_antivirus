package com.mipt.team4.antivirus_scanner_service.exception.scan.tika;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;

public class TikaDetectIOException extends RecoverableException {
  public TikaDetectIOException(String fileName, Throwable cause) {
    super("Failed to detect mimetype: file=" + fileName, cause);
  }
}
