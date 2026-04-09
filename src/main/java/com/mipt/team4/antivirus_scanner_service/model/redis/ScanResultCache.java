package com.mipt.team4.antivirus_scanner_service.model.redis;

import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import java.time.LocalDateTime;

public record ScanResultCache(
    ScanVerdict verdict,
    String detectedMimeType,
    long fileSize,
    String signatureVersion,
    LocalDateTime createdAt) {}
