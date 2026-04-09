package com.mipt.team4.antivirus_scanner_service.service.scan;

import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.dto.ScanTaskDto;
import com.mipt.team4.antivirus_scanner_service.service.s3.S3Service;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScanOrchestrator {
  private final S3Service s3Service;

  public void scan(ScanTaskDto scanTask) {
    if (scanTask.size() == 0) {
      ...
    }

    InputStream inputStream = s3Service.getFileStream(scanTask.s3Key());
    ScanContext context = createContext(scanTask, inputStream);
  }

  private ScanContext createContext(ScanTaskDto scanTask, InputStream inputStream) {
    return ScanContext.builder()
        .fileId(scanTask.fileId())
        .inputStream(inputStream)
        .hash(scanTask.hash())
        .originalName(scanTask.name())
        .declaredMimeType(scanTask.mimeType())
        .size(scanTask.size())
        .build();
  }
}
