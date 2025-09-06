package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.DownloadRequest;
import com.Rakumo.object.dto.DownloadResponse;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.DownloadManagerService;
import com.Rakumo.object.util.ChecksumUtils;
import com.Rakumo.object.util.ContentTypeResolver;
import com.Rakumo.object.util.FilePathUtils;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.internal.BoundedInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DownloadManagerServiceImpl implements DownloadManagerService {
    @Value("${storage.root}")
    private String baseStoragePath;

    @Override
    public DownloadResponse retrieveFile(DownloadRequest request)
            throws ObjectNotFoundException, IOException {

        // 1. Sanitize inputs first!
        String safeBucket = FilePathUtils.sanitize(request.getBucketName());
        String safeKey = FilePathUtils.sanitize(request.getObjectKey());
        String versionId = request.getVersionId() != null ?
                FilePathUtils.sanitize(request.getVersionId()) : "latest";

        // 2. Use utility for path resolution
        Path filePath = FilePathUtils.resolvePath(
                Path.of(baseStoragePath),
                safeBucket,
                safeKey,
                versionId
        );

        // 3. Verify file exists
        if (!Files.exists(filePath)) {
            throw new ObjectNotFoundException("File not found: " + filePath);
        }

        // 4. Get checksum for integrity
        String checksum = ChecksumUtils.sha256(filePath);

        // 5. Return complete response
        return DownloadResponse.builder()
                .bucketName(request.getBucketName()) // Original names for response
                .objectKey(request.getObjectKey())
                .versionId(request.getVersionId())
                .contentType(ContentTypeResolver.resolve(filePath))
                .checksum(checksum)
                .contentLength(Files.size(filePath))
                .lastModified(Files.getLastModifiedTime(filePath).toInstant())
                .dataStream(Files.newInputStream(filePath)) // Caller must handle closing!
                .build();
    }

    @Override
    public String generatePresignedUrl(DownloadRequest request, Duration expiry) {
        // Simple version for local development
        return String.format("/download/%s/%s?version=%s",
                expiry.toSeconds(),
                URLEncoder.encode(request.getBucketName(), StandardCharsets.UTF_8),
                URLEncoder.encode(request.getObjectKey(), StandardCharsets.UTF_8),
                request.getVersionId() != null ?
                        URLEncoder.encode(request.getVersionId(), StandardCharsets.UTF_8) : ""
        );
    }

    @Override
    public ChunkedStream streamFileRange(LocalObjectReference ref, long start, long end)
            throws IOException {

        // 1. Sanitize and build proper path
        String safeBucket = FilePathUtils.sanitize(ref.getBucketName());
        String safeKey = FilePathUtils.sanitize(ref.getObjectKey());
        String versionId = ref.getVersionId() != null ?
                FilePathUtils.sanitize(ref.getVersionId()) : "latest";

        Path filePath = FilePathUtils.resolvePath(
                Path.of(baseStoragePath),
                safeBucket,
                safeKey,
                versionId
        );

        // 2. Validate range
        long fileSize = Files.size(filePath);
        if (start < 0 || start >= fileSize) {
            throw new IllegalArgumentException("Invalid range start: " + start);
        }
        if (end >= fileSize) {
            end = fileSize - 1; // Adjust to file end
        }
        if (start > end) {
            throw new IllegalArgumentException("Invalid range: start > end");
        }

        // 3. Use efficient seeking with FileInputStream
        FileInputStream fileStream = new FileInputStream(filePath.toFile());
        FileChannel channel = fileStream.getChannel();
        channel.position(start); // doesn't read discarded bytes

        long chunkSize = end - start + 1;

        // 4. Wrap in auto-closing stream
        InputStream boundedStream = new BoundedInputStream(fileStream, (int)chunkSize);
        InputStream autoClosingStream = new FilterInputStream(boundedStream) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    fileStream.close(); // Ensure proper closure
                }
            }
        };

        return new ChunkedStream(autoClosingStream, (int) Math.min(chunkSize, Integer.MAX_VALUE));
    }
}