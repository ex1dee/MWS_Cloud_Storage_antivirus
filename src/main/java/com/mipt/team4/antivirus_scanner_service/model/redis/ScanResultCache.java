package com.mipt.team4.antivirus_scanner_service.model.redis;

import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ScanResultCache(
    ScanVerdict verdict, long fileSize, String signatureVersion, LocalDateTime createdAt) {}
