package com.mipt.team4.antivirus_scanner_service.model.context;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ScanContext(
    UUID fileId, String hash, String originalName, String declaredMimeType, long size) {}
