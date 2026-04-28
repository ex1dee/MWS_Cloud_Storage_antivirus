package com.mipt.team4.antivirus_scanner_service.service.scan.deep;

import com.mipt.team4.antivirus_scanner_service.model.context.ScanContext;
import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import java.io.InputStream;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepScanService {
  private final ClamavClient clamavClient;

  public ScanVerdict scan(ScanContext ctx, InputStream inputStream) {
    ScanResult result;

    try {
      result = clamavClient.scan(inputStream);
    } catch (Exception e) {
      log.error("Deep scan by ClamAV failed for file {}", ctx.fileId(), e);
      return ScanVerdict.UNKNOWN;
    }

    return getVerdict(ctx, result);
  }

  private ScanVerdict getVerdict(ScanContext ctx, ScanResult result) {
    if (result instanceof ScanResult.OK) {
      return ScanVerdict.CLEAN;
    }

    if (result instanceof ScanResult.VirusFound found) {
      log.warn("Virus detected! File ID: {}, Signals: {}", ctx.fileId(), found.getFoundViruses());
      return getVerdictFromVirusFound(found);
    }

    return ScanVerdict.UNKNOWN;
  }

  private ScanVerdict getVerdictFromVirusFound(ScanResult.VirusFound found) {
    var viruses = found.getFoundViruses().values().stream().flatMap(Collection::stream).toList();
    boolean isPasswordProtected = viruses.stream().anyMatch(v -> v.contains("Encrypted"));

    if (isPasswordProtected) {
      return ScanVerdict.PASSWORD_PROTECTED;
    }

    return ScanVerdict.INFECTED;
  }
}
