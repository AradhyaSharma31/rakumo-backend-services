package com.Rakumo.object.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UploadResponse {

    private String bucketName;
    private String objectKey;
    private String versionId;
    private String checksum;
    private long sizeBytes;
    private Instant uploadedAt;

}
