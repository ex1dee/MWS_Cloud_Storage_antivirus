package com.mipt.team4.antivirus_scanner_service.model.redis;

import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import lombok.Builder;

@Builder
public record ScanResultCache(ScanVerdict verdict, String signatureVersion) {}
