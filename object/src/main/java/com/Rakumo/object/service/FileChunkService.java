package com.Rakumo.object.service;

import com.Rakumo.object.exception.InvalidChunkException;
import com.Rakumo.object.model.FileChunkInfo;
import com.Rakumo.object.model.LocalObjectReference;

import java.time.Duration;
import java.util.List;

public interface FileChunkService {

    String initiateMultipartUpload(LocalObjectReference ref);

    void validateChunk(FileChunkInfo chunk) throws InvalidChunkException;

    List<FileChunkInfo> listChunks(String uploadId);

    void cleanupStaleChunks(Duration olderThan);

}
