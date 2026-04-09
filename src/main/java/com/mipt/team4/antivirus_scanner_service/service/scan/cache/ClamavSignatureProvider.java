package com.mipt.team4.antivirus_scanner_service.service.scan.cache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClamavSignatureProvider {
  private final ClamavClient clamavClient;
  private final AtomicReference<String> version = new AtomicReference<>("pending");

  @EventListener(ApplicationReadyEvent.class)
  @Scheduled(
      fixedRateString = "${antivirus.scheduling.signature-update.interval}",
      timeUnit = TimeUnit.MINUTES)
  public void updateVersion() {
    try {
      String fullVersion = clamavClient.version();
      String parsedVersion = parseVersion(fullVersion);

      version.set(parsedVersion);
      log.info("ClamAV signature version updated: {}", parsedVersion);
    } catch (Exception e) {
      log.error("ClamAV version check failed", e);
    }
  }

  public String getVersion() {
    return version.get();
  }

  private String parseVersion(String fullVersion) {
    if (fullVersion == null || !fullVersion.contains("/")) {
      log.warn("ClamAV returned unexpected version string: {}", fullVersion);
      return fullVersion != null ? fullVersion : "unknown";
    }

    String[] parts = fullVersion.split("/");
    return parts[0] + ":" + parts[1];
  }
}
