package com.Rakumo.object.service;

import com.Rakumo.object.dto.DeleteRequest;
import com.Rakumo.object.dto.UploadFileRequest;
import com.Rakumo.object.dto.UploadResponse;
import com.Rakumo.object.exception.*;
import com.Rakumo.object.model.FileChunkInfo;
import com.Rakumo.object.model.LocalObjectReference;

import java.io.IOException;

public interface UploadManagerService {

    UploadResponse handleRegularUpload(UploadFileRequest request)
            throws IOException, MetadataSyncException, MetadataServiceException;

    String initiateMultipartUpload(UploadFileRequest request);

    void processChunk(FileChunkInfo chunk)
            throws InvalidChunkException, IOException, ChecksumMismatchException;

    UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, IncompleteUploadException, ObjectNotFoundException,
            MetadataServiceException, ChecksumMismatchException;

    DeleteRequest deleteObject(LocalObjectReference ref)
            throws IOException, ObjectNotFoundException, MetadataServiceException;
}