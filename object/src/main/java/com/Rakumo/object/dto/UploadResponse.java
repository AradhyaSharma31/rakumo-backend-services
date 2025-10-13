package com.Rakumo.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class UploadResponse {
    private String bucketName;
    private String objectKey;
    private String versionId;
    private String checksum;
    private Long sizeBytes;
    private Instant uploadedAt;
}