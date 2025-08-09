package com.Rakumo.object.service;

import com.Rakumo.object.dto.UploadFileRequest;
import com.Rakumo.object.dto.UploadResponse;
import com.Rakumo.object.exception.IncompleteUploadException;
import com.Rakumo.object.exception.InvalidChunkException;
import com.Rakumo.object.exception.MetadataSyncException;
import com.Rakumo.object.model.FileChunkInfo;

import java.io.IOException;

public interface UploadManagerService {

    UploadResponse handleRegularUpload(UploadFileRequest request)
            throws IOException, MetadataSyncException;

    String initiateMultipartUpload(UploadFileRequest request);

    void processChunk(FileChunkInfo chunk)
            throws InvalidChunkException, IOException;

    UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, IncompleteUploadException;
}
