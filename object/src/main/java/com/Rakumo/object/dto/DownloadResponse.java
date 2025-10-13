// File: src/main/java/com/Rakumo/object/dto/DownloadResponse.java
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
    private InputStream dataStream;
    private String contentType;
    private String checksum;
    private Long contentLength;
    private Instant lastModified;
}