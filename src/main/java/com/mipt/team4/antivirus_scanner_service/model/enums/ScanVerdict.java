package com.mipt.team4.antivirus_scanner_service.model.enums;

public enum ScanVerdict {
  UNKNOWN,
  CLEAN,
  INFECTED,
  EMPTY_FILE,
  TOO_LARGE,
  PASSWORD_PROTECTED,
  RESOURCE_EXHAUSTED,
  CONTENT_MISMATCH,
  ERROR
}
