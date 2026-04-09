package com.mipt.team4.antivirus_scanner_service.exception.scan.tika;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;
import java.util.UUID;

public class TikaParseIOException extends RecoverableException {

  public TikaParseIOException(UUID fileId, Throwable cause) {
    super("Failed to parse file " + fileId, cause);
  }
}
