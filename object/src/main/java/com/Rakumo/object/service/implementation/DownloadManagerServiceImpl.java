package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.DownloadRequest;
import com.Rakumo.object.dto.DownloadResponse;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.DownloadManagerService;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.service.MetadataService;
import io.netty.handler.stream.ChunkedStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DownloadManagerServiceImpl implements DownloadManagerService {

    private final FileStorageService fileStorageService;
    private final MetadataService metadataService;

    @Value("${storage.root}")
    private String baseStoragePath;

    @Override
    public DownloadResponse retrieveFile(DownloadRequest request)
            throws ObjectNotFoundException, IOException {

        // 1. Resolve object reference
        LocalObjectReference ref = LocalObjectReference.builder()
                .bucketName(request.getBucketName())
                .objectKey(request.getObjectKey())
                .versionId(request.getVersionId())
                .build();

        // 2. Verify file exists
        Path filePath = Path.of(baseStoragePath, ref.getBucketName(), ref.getObjectKey());
        if (!Files.exists(filePath)) {
            throw new ObjectNotFoundException("File not found: " + filePath);
        }

        // 3. Return download response
        return DownloadResponse.builder()
                .bucketName(ref.getBucketName())
                .objectKey(ref.getObjectKey())
                .versionId(ref.getVersionId())
                .contentLength(Files.size(filePath))
                .lastModified(Files.getLastModifiedTime(filePath).toInstant())
//                .content(Files.newInputStream(filePath)) // Caller must close this stream
                .build();
    }

    @Override
    public String generatePresignedUrl(DownloadRequest request, Duration expiry) {
        // Simple version for local development
        return String.format("/download/%s/%s?version=%s",
                request.getBucketName(),
                request.getObjectKey(),
                request.getVersionId());
    }

    @Override
    public ChunkedStream streamFileRange(LocalObjectReference ref, long start, long end)
            throws IOException {
        Path filePath = Path.of(baseStoragePath, ref.getBucketName(), ref.getObjectKey());
        InputStream stream = Files.newInputStream(filePath);
        stream.skip(start);
        return new ChunkedStream(stream, (int)(end - start + 1));
    }
}