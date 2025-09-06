package com.Rakumo.object.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
@AllArgsConstructor
public class LocalObjectReference {

    private String bucketName;
    private String objectKey;
    private String versionId;
    private String checksum; // SHA-256
    private Path physicalPath;
    private long size;
}
