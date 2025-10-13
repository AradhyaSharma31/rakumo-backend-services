// FileChunkService.java
package com.Rakumo.object.service;

import com.Rakumo.object.entity.FileChunkInfo;
import com.Rakumo.object.entity.LocalObjectReference;
import com.Rakumo.object.exception.InvalidChunkException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public interface FileChunkService {
    String initiateMultipartUpload(LocalObjectReference ref);
    void validateChunk(FileChunkInfo chunk) throws InvalidChunkException, IOException;
    List<FileChunkInfo> listChunks(String uploadId);
    void cleanupStaleChunks(Duration olderThan);
    void cleanupUpload(String uploadId) throws IOException;
}
