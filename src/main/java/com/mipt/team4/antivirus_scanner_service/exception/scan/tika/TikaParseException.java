package com.mipt.team4.antivirus_scanner_service.exception.scan.tika;

import com.mipt.team4.antivirus_scanner_service.exception.base.BaseException;
import java.util.UUID;

public class TikaParseException extends BaseException {
  public TikaParseException(UUID fileId, Throwable cause) {
    super("Failed to parse file " + fileId, cause);
  }
}
