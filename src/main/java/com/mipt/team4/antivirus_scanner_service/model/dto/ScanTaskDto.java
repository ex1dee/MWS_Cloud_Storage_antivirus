package com.mipt.team4.antivirus_scanner_service.model.dto;

import java.util.UUID;

public record ScanTaskDto(
    UUID fileId, String name, String hash, String mimeType, String s3Key, long size) {}
