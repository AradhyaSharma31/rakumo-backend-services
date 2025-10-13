package com.Rakumo.object.service;

import com.Rakumo.object.dto.UploadRequest;
import com.Rakumo.object.dto.UploadResponse;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.MetadataServiceException;

import java.io.IOException;
import java.io.InputStream;

public interface UploadManagerService {
    UploadResponse handleRegularUpload(UploadRequest request, InputStream fileData)
            throws IOException, MetadataServiceException;

    String initiateMultipartUpload(UploadRequest request);

    void uploadChunk(String uploadId, int chunkIndex, InputStream chunkData)
            throws IOException;

    UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, MetadataServiceException, ChecksumMismatchException;

    void abortMultipartUpload(String uploadId) throws IOException;
}
