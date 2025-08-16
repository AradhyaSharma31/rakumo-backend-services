package com.Rakumo.object.service;

import com.Rakumo.object.dto.DownloadRequest;
import com.Rakumo.object.dto.DownloadResponse;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.model.LocalObjectReference;
import io.netty.handler.stream.ChunkedStream;

import java.io.IOException;
import java.time.Duration;

public interface DownloadManagerService {
    /**
     * Retrieves a file with metadata
     */
    DownloadResponse retrieveFile(DownloadRequest request)
            throws ObjectNotFoundException, IOException;

    /**
     * Generates pre-signed download URL
     */
    String generatePresignedUrl(DownloadRequest request, Duration expiry);

    /**
     * Streams file content by range
     */
    ChunkedStream streamFileRange(LocalObjectReference ref, long start, long end)
            throws IOException;
}
