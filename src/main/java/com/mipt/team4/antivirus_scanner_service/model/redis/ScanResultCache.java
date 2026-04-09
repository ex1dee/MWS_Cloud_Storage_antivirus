package com.mipt.team4.antivirus_scanner_service.model.redis;

import com.mipt.team4.antivirus_scanner_service.model.enums.ScanResult;
import java.time.LocalDateTime;

public record ScanResultCache(
    ScanResult status,
    String detectedMimeType,
    long fileSize,
    String signatureVersion,
    LocalDateTime createdAt) {}
