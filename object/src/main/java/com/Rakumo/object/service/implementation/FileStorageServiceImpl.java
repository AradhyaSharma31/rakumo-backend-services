package com.Rakumo.object.service.implementation;

import com.Rakumo.object.entity.RegularObjectEntity;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.MetadataServiceException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.grpc.MetadataGrpcClient;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final RegularObjectRepository regularObjectRepository;
    private final MetadataGrpcClient metadataGrpcClient;
    private final ChecksumUtils checksumUtils;

    @Value("${storage.root:./storage}")
    private String storageRoot;

    @Override
    public RegularObjectEntity storeFile(String ownerId, String bucketId, String objectKey, InputStream inputStream,
                                         String contentType, String expectedChecksum)
            throws IOException, ChecksumMismatchException {

        // throw error if file with same checksum already exists
        if (regularObjectRepository.existsByChecksumAndBucketId(expectedChecksum, bucketId)) {
            throw new IOException("File already exists inside the bucket");
        }

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
            Path finalPath = resolveFilePath(ownerId, bucketId, objectKey, actualChecksum);
            Files.createDirectories(finalPath.getParent());

            // Move to final location
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

            // Create and save entity FIRST
            RegularObjectEntity entity = new RegularObjectEntity();
            entity.setOwnerId(ownerId);
            entity.setBucketName(bucketId);
            entity.setObjectKey(objectKey);
            entity.setVersionId(versionId);
            entity.setFileName(Paths.get(objectKey).getFileName().toString());
            entity.setChecksum(actualChecksum);
            entity.setSizeBytes(fileSize);
            entity.setContentType(detectedContentType);
            entity.setPhysicalPath(finalPath.toString());

            RegularObjectEntity savedEntity = regularObjectRepository.save(entity);

            // call metadata service with the saved entity's ID
            try {
                metadataGrpcClient.createObject(
                        savedEntity.getId().toString(),
                        bucketId,
                        objectKey,
                        versionId,
                        actualChecksum,
                        fileSize
                );
            } catch (MetadataServiceException e) {
                // Rollback BOTH file and database entry
                Files.deleteIfExists(finalPath);
                regularObjectRepository.delete(savedEntity);
                log.error("Failed to create object in metadata service: {}", e.getMessage());
                throw new IOException("Failed to create object in metadata service: " + e.getMessage(), e);
            }

            log.info("Stored file: {}/{} ({} bytes)", bucketId, objectKey, fileSize);
            return savedEntity;
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    @Override
    public Resource retrieveFile(String bucketName, String objectKey, String versionId)
            throws ObjectNotFoundException, IOException {
        RegularObjectEntity entity = regularObjectRepository.findByBucketAndKeyAndVersion(bucketName, objectKey, versionId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        String.format("Object not found: %s/%s (version: %s)", bucketName, objectKey, versionId)));

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
    public void deleteFile(String ownerId, String bucketName, String objectKey, String fileId)
            throws ObjectNotFoundException, IOException, MetadataServiceException {
        RegularObjectEntity entity = regularObjectRepository.findById(UUID.fromString(fileId))
                .orElseThrow(() -> new ObjectNotFoundException(
                        String.format("Object not found: %s/%s (id: %s)", bucketName, objectKey, fileId)));

        String bucketObjectId = metadataGrpcClient.getBucketObjects(bucketName).getObjectsList()
                .stream()
                .filter(obj -> obj.getObjectKey().equals(objectKey))
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException(
                        String.format("Object metadata not found in bucket: %s/%s (version: %s)", bucketName, objectKey, entity.getVersionId())))
                .getId();

        // Create file hash
        String fileHash = regularObjectRepository.getChecksumById(UUID.fromString(fileId));

        // Resolve the file path using checksum
        Path filePath = FilePathUtils.resolveRegularFilePath(ownerId, bucketName, objectKey, fileHash);

        if (!Files.exists(filePath)) {
            throw new ObjectNotFoundException("File not found: " + filePath);
        }

        try {
            // delete object from bucket
            metadataGrpcClient.deleteObject(bucketName, bucketObjectId);

            // delete the file from db
            regularObjectRepository.delete(entity);

            // Delete the file from storage
            Files.delete(filePath);
            log.info("Deleted file: {}", filePath);

            // Delete empty parent directories (checksum dirs)
            deleteEmptyParentDirs(filePath);

        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw new IOException("Failed to delete file: " + e.getMessage(), e);
        } catch (MetadataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recursively delete empty parent directories up to the bucket level
     */
    private void deleteEmptyParentDirs(Path filePath) throws IOException {
        Path currentDir = filePath.getParent();
        Path bucketLevel = filePath.getParent().getParent().getParent().getParent(); // Up to bucket level

        while (currentDir != null && Files.exists(currentDir) &&
                !currentDir.equals(bucketLevel) && !currentDir.equals(bucketLevel.getParent())) {

            try {
                // Check if directory is empty
                try (Stream<Path> entries = Files.list(currentDir)) {
                    if (entries.findAny().isPresent()) {
                        // Directory is not empty, stop deletion
                        break;
                    }
                }

                // Delete empty directory
                Files.delete(currentDir);
                log.debug("Deleted empty directory: {}", currentDir);

                // Move up to parent directory
                currentDir = currentDir.getParent();

            } catch (IOException e) {
                log.warn("Failed to delete directory {}: {}", currentDir, e.getMessage());
                break;
            }
        }
    }

    private Path resolveFilePath(String ownerId, String bucketName, String objectKey, String fileHash) {
        return FilePathUtils.resolveRegularFilePath(
                FilePathUtils.sanitize(ownerId),
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
