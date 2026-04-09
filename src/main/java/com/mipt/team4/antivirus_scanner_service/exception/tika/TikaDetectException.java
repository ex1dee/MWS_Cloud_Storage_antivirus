package com.mipt.team4.antivirus_scanner_service.exception.tika;

import com.mipt.team4.antivirus_scanner_service.exception.base.BaseException;

public class TikaDetectException extends BaseException {
  public TikaDetectException(String fileName, Throwable cause) {
    super("Failed to detect mimetype: file=" + fileName, cause);
  }
}
