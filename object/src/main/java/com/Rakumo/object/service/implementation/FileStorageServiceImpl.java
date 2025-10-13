package com.Rakumo.object.service.implementation;

import com.Rakumo.object.entity.RegularObjectEntity;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.repository.RegularObjectRepository;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.util.ChecksumUtils;
import com.Rakumo.object.util.ContentTypeResolver;
import com.Rakumo.object.util.FilePathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final RegularObjectRepository regularObjectRepository;
    private final ChecksumUtils checksumUtils;

    @Value("${storage.root:./storage}")
    private String storageRoot;

    @Override
    public RegularObjectEntity storeFile(String ownerId, String bucketName, String objectKey, InputStream inputStream,
                                         String contentType, String expectedChecksum)
            throws IOException, ChecksumMismatchException {
        Path tempPath = createTempFile();
        try {
            // Copy to temp file first
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);

            // Calculate actual checksum
            String actualChecksum = checksumUtils.sha256(tempPath);

            // Verify checksum if provided
            if (expectedChecksum != null && !expectedChecksum.equals(actualChecksum)) {
                throw new ChecksumMismatchException(
                        String.format("Checksum mismatch. Expected: %s, Actual: %s", expectedChecksum, actualChecksum));
            }

            // Detect content type if not provided
            String detectedContentType = contentType != null ? contentType :
                    ContentTypeResolver.resolveFromFilename(objectKey);

            // Create file metadata
            String versionId = UUID.randomUUID().toString();
            long fileSize = Files.size(tempPath);

            // Resolve final path
            Path finalPath = resolveFilePath(ownerId, bucketName, objectKey, actualChecksum);
            Files.createDirectories(finalPath.getParent());

            // Move to final location
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

            // Create and save entity
            RegularObjectEntity entity = new RegularObjectEntity();
            entity.setOwnerId(ownerId); // TODO: Get from authenticated context
            entity.setBucketName(bucketName);
            entity.setObjectKey(objectKey);
            entity.setVersionId(versionId);
            entity.setFileName(Paths.get(objectKey).getFileName().toString());
            entity.setChecksum(actualChecksum);
            entity.setSizeBytes(fileSize);
            entity.setContentType(detectedContentType);
            entity.setPhysicalPath(finalPath.toString());

            RegularObjectEntity savedEntity = regularObjectRepository.save(entity);
            log.info("Stored file: {}/{} ({} bytes)", bucketName, objectKey, fileSize);
            return savedEntity;
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    @Override
    public Resource retrieveFile(String bucketName, String objectKey, String versionId)
            throws ObjectNotFoundException, IOException {
        RegularObjectEntity entity = findObjectEntity(bucketName, objectKey, versionId);
        Path filePath = Paths.get(entity.getPhysicalPath());

        if (!Files.exists(filePath)) {
            throw new ObjectNotFoundException("Physical file not found: " + filePath);
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ObjectNotFoundException("Cannot read file: " + filePath);
        }

        return resource;
    }

    @Override
    @Transactional
    public void deleteFile(String bucketName, String objectKey, String versionId)
            throws ObjectNotFoundException, IOException {
        RegularObjectEntity entity = findObjectEntity(bucketName, objectKey, versionId);
        Path filePath = Paths.get(entity.getPhysicalPath());

        // Delete physical file
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete database record
        regularObjectRepository.delete(entity);
        log.info("Deleted file: {}/{}", bucketName, objectKey);
    }

    private RegularObjectEntity findObjectEntity(String bucketName, String objectKey, String versionId)
            throws ObjectNotFoundException {
        RegularObjectEntity entity;
        if (versionId != null) {
            entity = regularObjectRepository.findByBucketAndKeyAndVersion(bucketName, objectKey, versionId)
                    .orElseThrow(() -> new ObjectNotFoundException(
                            String.format("Object not found: %s/%s (version: %s)", bucketName, objectKey, versionId)));
        } else {
            entity = regularObjectRepository.findByBucketNameAndObjectKey(bucketName, objectKey)
                    .orElseThrow(() -> new ObjectNotFoundException(
                            String.format("Object not found: %s/%s", bucketName, objectKey)));
        }
        return entity;
    }

    private Path resolveFilePath(String ownerId, String bucketName, String objectKey, String fileHash) {
        return FilePathUtils.resolveRegularFilePath(
                FilePathUtils.sanitize(ownerId), // userId - TODO: get from authenticated context
                FilePathUtils.sanitize(bucketName),
                objectKey,
                fileHash
        );
    }


    private Path createTempFile() throws IOException {
        Path tempDir = Paths.get(storageRoot, ".temp");
        Files.createDirectories(tempDir);
        return Files.createTempFile(tempDir, "upload-", ".tmp");
    }
}
