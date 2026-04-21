package com.mipt.team4.antivirus_scanner_service.exception.scan;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanStage;
import java.util.UUID;

public class ScanExecutionException extends RecoverableException {
  public ScanExecutionException(ScanStage stage, UUID fileId, Throwable cause) {
    super("Failed to execute scan stage %s for file %s".formatted(stage.name(), fileId), cause);
  }
}
