package com.Rakumo.object.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class PreSignedUrlRequest {
    private String bucketName;
    private String objectKey;
    private String versionId;
    private PreSignedUrlOperation operation;
    private Duration expiration;
    private String contentType;

    public enum PreSignedUrlOperation {
        DOWNLOAD, UPLOAD, DELETE
    }
}