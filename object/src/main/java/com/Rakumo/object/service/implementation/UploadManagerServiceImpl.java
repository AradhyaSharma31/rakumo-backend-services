package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.*;
import com.Rakumo.object.exception.*;
import com.Rakumo.object.model.*;
import com.Rakumo.object.service.FileChunkService;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.service.MetadataService;
import com.Rakumo.object.service.UploadManagerService;
import com.Rakumo.object.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Requ iredArgsConstructor
public class UploadManagerServiceImpl implements UploadManagerService {
    private static final Logger log = LoggerFactory.getLogger(UploadManagerServiceImpl.class);

    private final FileStorageService fileStorageService;
    private final FileChunkService fileChunkService;
    private final MetadataService metadataService;
    private final ChecksumUtils checksumUtils;

    @Override
    public UploadResponse handleRegularUpload(UploadFileRequest request)
            throws IOException, MetadataSyncException {

        // 1. Generate object reference
        LocalObjectReference ref = createObjectReference(request);

        // 2. Calculate checksum while streaming
        try (InputStream inputStream = request.getFile().getInputStream()) {
            String checksum = checksumUtils.sha256(inputStream);
            ref.setChecksum(checksum);

            // 3. Store file
            try (InputStream freshStream = request.getFile().getInputStream()) {
                fileStorageService.storeCompleteFile(ref, freshStream);
            } catch (ChecksumMismatchException e) {
                throw new RuntimeException(e);
            }
        }

        // 4. Record metadata
        metadataService.recordMetadata(ref);

        return new UploadResponse(
                ref.getBucketName(),
                ref.getObjectKey(),
                ref.getVersionId(),
                ref.getChecksum(),
                request.getFile().getSize(),
                Instant.now()
        );
    }

    @Override
    public String initiateMultipartUpload(UploadFileRequest request) {
        LocalObjectReference ref = createObjectReference(request);
        return fileChunkService.initiateMultipartUpload(ref);
    }

    @Override
    public void processChunk(FileChunkInfo chunk)
            throws InvalidChunkException, IOException {

        // 1. Validate chunk sequence
        fileChunkService.validateChunk(chunk);

        // 2. Verify chunk checksum if provided
        if (chunk.getChecksum() != null) {
            String actualChecksum = checksumUtils.sha256(chunk.getInputStream());
            if (!actualChecksum.equals(chunk.getChecksum())) {
                throw new InvalidChunkException("Chunk checksum mismatch");
            }
        }

        // 3. Store chunk
        fileStorageService.storeChunk(chunk);
    }

    @Override
    public UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, IncompleteUploadException, ObjectNotFoundException, MetadataSyncException {

        // 1. Get chunk metadata
        List<FileChunkInfo> chunks = fileChunkService.listChunks(uploadId);
        if (chunks.isEmpty()) {
            throw new IncompleteUploadException("No chunks found for upload: " + uploadId);
        }

        // 2. Create final object reference
        FileChunkInfo firstChunk = chunks.get(0);
        LocalObjectReference ref = new LocalObjectReference(
                firstChunk.getBucketName(),
                firstChunk.getObjectKey(),
                UUID.randomUUID().toString(),
                null,
                null
        );

        // 3. Assemble and verify final file
        fileStorageService.assembleChunks(uploadId, ref);

        // 4. Calculate final checksum
        String finalChecksum = checksumUtils.sha256(fileStorageService.retrieveFile(ref));
        ref.setChecksum(finalChecksum);

        // 5. Record metadata
        metadataService.recordMetadata(ref);

        // 6. Cleanup
        fileChunkService.cleanupStaleChunks(Duration.ZERO); // Immediate cleanup

        return new UploadResponse(
                ref.getBucketName(),
                ref.getObjectKey(),
                ref.getVersionId(),
                ref.getChecksum(),
                chunks.stream().mapToLong(FileChunkInfo::getChunkSize).sum(),
                Instant.now()
        );
    }

    // ================ Helper Methods ================
    private LocalObjectReference createObjectReference(UploadFileRequest request) {
        return new LocalObjectReference(
                request.getBucketName(),
                request.getObjectKey(),
                UUID.randomUUID().toString(),
                null,
                null
        );
    }
}
