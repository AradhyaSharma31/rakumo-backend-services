/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// DownloadManagerServiceImpl.java (Fixed)
package com.rakumo.object.service.implementation;

import com.rakumo.object.dto.DownloadRequest;
import com.rakumo.object.dto.DownloadResponse;
import com.rakumo.object.exception.ObjectNotFoundException;
import com.rakumo.object.service.DownloadManagerService;
import com.rakumo.object.service.FileStorageService;
import com.rakumo.object.util.ChecksumUtils;
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
