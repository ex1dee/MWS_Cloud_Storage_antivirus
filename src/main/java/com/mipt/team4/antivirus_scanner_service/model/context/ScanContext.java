package com.mipt.team4.antivirus_scanner_service.model.context;

import java.io.InputStream;
import java.util.UUID;

public record ScanContext(
    UUID fileId,
    InputStream inputStream,
    String hash,
    String originalName,
    String declaredMimeType,
    long size) {}
