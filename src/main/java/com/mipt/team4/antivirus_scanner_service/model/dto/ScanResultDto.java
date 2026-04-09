package com.mipt.team4.antivirus_scanner_service.model.dto;

import com.mipt.team4.antivirus_scanner_service.model.enums.ScanVerdict;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ScanResultDto(UUID fileId, ScanVerdict verdict) {}
