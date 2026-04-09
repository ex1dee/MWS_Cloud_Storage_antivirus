package com.mipt.team4.antivirus_scanner_service.exception.s3;

import com.mipt.team4.antivirus_scanner_service.exception.base.FatalException;

public class DownloadFileException extends FatalException {
  public DownloadFileException(String s3Key, Throwable cause) {
    super("Failed to download file from S3: " + fileId, cause);
  }
}
