package com.mipt.team4.antivirus_scanner_service.exception.s3;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;
import java.util.UUID;

public class CloseFileStreamException extends RecoverableException {
  public CloseFileStreamException(UUID fileId, Throwable cause) {
    super("Failed to close stream of file " + fileId, cause);
  }
}
