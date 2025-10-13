// DownloadManagerServiceImpl.java (Fixed)
package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.DownloadRequest;
import com.Rakumo.object.dto.DownloadResponse;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.service.DownloadManagerService;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.util.ChecksumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadManagerServiceImpl implements DownloadManagerService {

    private final FileStorageService fileStorageService;
    private final ChecksumUtils checksumUtils;

    @Override
    public DownloadResponse retrieveFile(DownloadRequest request) throws ObjectNotFoundException, IOException {
        // Use FileStorageService to get the resource
        Resource resource = fileStorageService.retrieveFile(
                request.getBucketName(),
                request.getObjectKey(),
                request.getVersionId()
        );

        Path filePath = Paths.get(resource.getURI());

        // Get file metadata
        String checksum = checksumUtils.sha256(filePath);
        String contentType = Files.probeContentType(filePath);
        long contentLength = Files.size(filePath);
        Instant lastModified = Files.getLastModifiedTime(filePath).toInstant();

        return DownloadResponse.builder()
                .bucketName(request.getBucketName())
                .objectKey(request.getObjectKey())
                .versionId(request.getVersionId())
                .dataStream(resource.getInputStream())
                .contentType(contentType)
                .checksum(checksum)
                .contentLength(contentLength)
                .lastModified(lastModified)
                .build();
    }
}
