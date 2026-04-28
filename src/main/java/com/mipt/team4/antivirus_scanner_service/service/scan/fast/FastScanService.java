package com.mipt.team4.antivirus_scanner_service.service.scan.fast;

import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import com.mipt.team4.antivirus_scanner_service.utils.MimeTypeNormalizer;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastScanService {
  private final DetectMimeTypeService detectMimeTypeService;

  public ScanVerdict scan(ScanContext ctx, InputStream inputStream) {
    String originalName = ctx.originalName();

    String mimeTypeByName =
        MimeTypeNormalizer.normalize(detectMimeTypeService.detectByName(originalName));
    String mimeTypeByStream =
        MimeTypeNormalizer.normalize(
            detectMimeTypeService.detectByStream(inputStream, originalName));

    if (!mimeTypeByStream.equals(mimeTypeByName)) {
      return ScanVerdict.CONTENT_MISMATCH;
    }

    return ScanVerdict.CLEAN;
  }
}
