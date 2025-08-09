package com.Rakumo.object.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class FileChunkInfo {

    private String uploadId;
    private int chunkIndex;
    private long chunkOffSet; // Byte position in final file
    private long chunkSize;
    private String checksum;
    private Path tempStoredPath; // chunk is stored temporarily saved

    @Builder.Default
    private boolean isLastChunk = false;
}
