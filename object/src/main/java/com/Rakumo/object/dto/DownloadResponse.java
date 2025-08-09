package com.Rakumo.object.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.time.Instant;

@Data
@Builder
public class DownloadResponse {
    private String bucketName;
    private String objectKey;
    private String versionId;
    private InputStream dataStream;  // Actual file content
    private String contentType;
    private String checksum;         // SHA-256
    private long contentLength;
    private Instant lastModified;
}