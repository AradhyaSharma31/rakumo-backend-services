package com.Rakumo.object.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileChunkInfo {
    private String uploadId;
    private int chunkIndex;
    private long chunkSize;
    private String checksum;
    private String bucketName;
    private String objectKey;
    private String filePath;     // Path to the chunk file on disk
    private Instant uploadedAt;

    @Builder.Default
    private boolean isLastChunk = false;
}
