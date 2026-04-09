package com.mipt.team4.antivirus_scanner_service.service.scan.fast;

import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import com.mipt.team4.antivirus_scanner_service.utils.MimeTypeNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FastScanService {
  private final DetectMimeTypeService detectMimeTypeService;

  public ScanVerdict scan(ScanContext ctx) {
    String originalName = ctx.originalName();
    String declaredMimeType = MimeTypeNormalizer.normalize(ctx.declaredMimeType());

    String mimeTypeByName =
        MimeTypeNormalizer.normalize(detectMimeTypeService.detectByName(originalName));
    if (!mimeTypeByName.equals(declaredMimeType)) {
      return ScanVerdict.CONTENT_MISMATCH;
    }

    String mimeTypeByStream =
        MimeTypeNormalizer.normalize(
            detectMimeTypeService.detectByStream(ctx.inputStream(), originalName, ctx.fileId()));
    if (!mimeTypeByStream.equals(mimeTypeByName)) {
      return ScanVerdict.CONTENT_MISMATCH;
    }

    return ScanVerdict.CLEAN;
  }
}
