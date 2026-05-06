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

import com.rakumo.object.presigned.*;
import com.rakumo.object.dto.PreSignedUrlRequest;
import com.rakumo.object.dto.PreSignedUrlResponse;
import com.rakumo.object.service.PreSignedUrlService;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class PreSignedUrlGrpcService extends PreSignedUrlServiceProtoGrpc.PreSignedUrlServiceProtoImplBase {

    private final PreSignedUrlService preSignedUrlService;

    @Override
    public void generatePreSignedUrl(GeneratePreSignedUrlRequest request,
                                     StreamObserver<GeneratePreSignedUrlResponse> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Convert to DTO
            PreSignedUrlRequest preSignedRequest = PreSignedUrlRequest.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .versionId(request.getVersionId().isEmpty() ? null : request.getVersionId())
                    .operation(convertOperation(request.getOperation()))
                    .expiration(request.getExpirationSeconds() > 0 ?
                            Duration.ofSeconds(request.getExpirationSeconds()) : null)
                    .contentType(request.getContentType().isEmpty() ? null : request.getContentType())
                    .build();

            // Generate pre-signed URL
            PreSignedUrlResponse response = preSignedUrlService.generatePreSignedUrl(preSignedRequest);

            // Build gRPC response
            GeneratePreSignedUrlResponse grpcResponse = GeneratePreSignedUrlResponse.newBuilder()
                    .setPreSignedUrl(response.getPreSignedUrl())
                    .setBucketName(response.getBucketName())
                    .setObjectKey(response.getObjectKey())
                    .setVersionId(response.getVersionId() != null ? response.getVersionId() : "")
                    .setOperation(convertOperation(response.getOperation()))
                    .setExpiration(Timestamp.newBuilder()
                            .setSeconds(response.getExpiration().getEpochSecond())
                            .setNanos(response.getExpiration().getNano())
                            .build())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to generate pre-signed URL: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void validatePreSignedUrl(ValidatePreSignedUrlRequest request,
                                     StreamObserver<ValidatePreSignedUrlResponse> responseObserver) {
        try {
            // Validate request
            if (request.getUrl().isEmpty() || request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("URL, bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Validate the pre-signed URL
            boolean isValid = preSignedUrlService.validatePreSignedUrl(
                    request.getUrl(),
                    request.getBucketName(),
                    request.getObjectKey()
            );

            ValidatePreSignedUrlResponse response = ValidatePreSignedUrlResponse.newBuilder()
                    .setIsValid(isValid)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Failed to validate pre-signed URL", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to validate pre-signed URL: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private PreSignedUrlRequest.PreSignedUrlOperation convertOperation(Operation operation) {
        switch (operation) {
            case DOWNLOAD:
                return PreSignedUrlRequest.PreSignedUrlOperation.DOWNLOAD;
            case UPLOAD:
                return PreSignedUrlRequest.PreSignedUrlOperation.UPLOAD;
            case DELETE:
                return PreSignedUrlRequest.PreSignedUrlOperation.DELETE;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    private Operation convertOperation(PreSignedUrlRequest.PreSignedUrlOperation operation) {
        switch (operation) {
            case DOWNLOAD:
                return Operation.DOWNLOAD;
            case UPLOAD:
                return Operation.UPLOAD;
            case DELETE:
                return Operation.DELETE;
            default:
                return Operation.UNRECOGNIZED;
        }
    }
}
