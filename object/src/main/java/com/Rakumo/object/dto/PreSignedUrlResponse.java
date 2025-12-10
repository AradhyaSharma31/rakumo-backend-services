package com.Rakumo.object.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PreSignedUrlResponse {
    private String preSignedUrl;
    private String bucketName;
    private String objectKey;
    private String versionId;
    private PreSignedUrlRequest.PreSignedUrlOperation operation;
    private Instant expiration;
    private String uploadId; // For multipart uploads
}