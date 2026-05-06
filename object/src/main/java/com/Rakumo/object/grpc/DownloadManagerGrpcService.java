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

package com.rakumo.object.grpc;

import com.rakumo.object.download.*;
import com.rakumo.object.dto.DownloadRequest;
import com.rakumo.object.dto.DownloadResponse;
import com.rakumo.object.service.DownloadManagerService;
import com.rakumo.object.exception.ObjectNotFoundException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class DownloadManagerGrpcService extends DownloadManagerServiceProtoGrpc.DownloadManagerServiceProtoImplBase {

    private final DownloadManagerService downloadManagerService;

    @Override
    public void retrieveFile(DownloadRequestMessage request,
                             StreamObserver<DownloadResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Convert to DTO
            DownloadRequest downloadRequest = DownloadRequest.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .versionId(request.getVersionId().isEmpty() ? null : request.getVersionId())
                    .build();

            // Process download
            DownloadResponse response = downloadManagerService.retrieveFile(downloadRequest);

            // Convert file stream to bytes (for small files)
            // For large files, consider using streaming RPC
            byte[] fileData = response.getDataStream().readAllBytes();

            DownloadResponseMessage responseMessage = DownloadResponseMessage.newBuilder()
                    .setBucketName(response.getBucketName())
                    .setObjectKey(response.getObjectKey())
                    .setVersionId(response.getVersionId() != null ? response.getVersionId() : "")
                    .setContentLength(response.getContentLength())
                    .setContentType(response.getContentType() != null ? response.getContentType() : "")
                    .setChecksum(response.getChecksum() != null ? response.getChecksum() : "")
                    .setLastModified(Timestamp.newBuilder()
                            .setSeconds(response.getLastModified().getEpochSecond())
                            .setNanos(response.getLastModified().getNano())
                            .build())
                    .setFileData(ByteString.copyFrom(fileData))
                    .build();

            responseObserver.onNext(responseMessage);
            responseObserver.onCompleted();

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("File not found: " + e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("Download failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Download failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in file download", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
