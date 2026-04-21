package com.mipt.team4.antivirus_scanner_service.exception.scan;

import com.mipt.team4.antivirus_scanner_service.exception.base.RecoverableException;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanStage;
import java.util.UUID;

public class ScanStageTimeoutException extends RecoverableException {

  public ScanStageTimeoutException(ScanStage stage, UUID fileId, Throwable cause) {
    super("Scan stage %s timed out for file %s".formatted(stage.name(), fileId), cause);
  }
}
