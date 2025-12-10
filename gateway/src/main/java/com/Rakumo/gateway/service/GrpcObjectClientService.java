package com.Rakumo.gateway.service;

import com.Rakumo.object.download.*;
import com.Rakumo.object.storage.*;
import com.Rakumo.object.upload.*;
import com.Rakumo.object.presigned.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcObjectClientService {

    @GrpcClient("object-service")
    private FileStorageServiceProtoGrpc.FileStorageServiceProtoBlockingStub fileStorageStub;

    @GrpcClient("object-service")
    private UploadManagerServiceProtoGrpc.UploadManagerServiceProtoBlockingStub uploadManagerStub;

    @GrpcClient("object-service")
    private DownloadManagerServiceProtoGrpc.DownloadManagerServiceProtoBlockingStub downloadManagerStub;

    @GrpcClient("object-service")
    private PreSignedUrlServiceProtoGrpc.PreSignedUrlServiceProtoBlockingStub preSignedUrlStub;

    // ========== FILE STORAGE OPERATIONS ==========

    public StoreFileResponseMessage storeFile(StoreFileRequestMessage request) {
        try {
            log.info("Storing file: {}/{}", request.getBucketName(), request.getObjectKey());
            return fileStorageStub.storeFile(request);
        } catch (StatusRuntimeException e) {
            log.error("File storage failed: {}", e.getStatus());
            throw new RuntimeException("File storage service unavailable: " + e.getStatus().getDescription());
        }
    }

    public java.util.Iterator<FileChunkMessage> retrieveFileStream(RetrieveFileRequestMessage request) {
        try {
            log.info("Retrieving file stream: {}/{}", request.getBucketName(), request.getObjectKey());
            return fileStorageStub.retrieveFileStream(request);
        } catch (StatusRuntimeException e) {
            log.error("File stream retrieval failed: {}", e.getStatus());
            throw new RuntimeException("File storage service unavailable: " + e.getStatus().getDescription());
        }
    }

    public DeleteFileResponseMessage deleteFile(DeleteFileRequestMessage request) {
        try {
            log.info("Deleting file: {}/{}", request.getBucketName(), request.getObjectKey());
            return fileStorageStub.deleteFile(request);
        } catch (StatusRuntimeException e) {
            log.error("File deletion failed: {}", e.getStatus());
            throw new RuntimeException("File storage service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== UPLOAD OPERATIONS ==========

    public UploadResponseMessage handleRegularUpload(UploadFileRequestMessage request) {
        try {
            log.info("Regular upload: {}/{}", request.getBucketName(), request.getObjectKey());
            return uploadManagerStub.handleRegularUpload(request);
        } catch (StatusRuntimeException e) {
            log.error("Regular upload failed: {}", e.getStatus());
            throw new RuntimeException("Upload service unavailable: " + e.getStatus().getDescription());
        }
    }

    public InitiateMultipartResponseMessage initiateMultipartUpload(InitiateMultipartRequestMessage request) {
        try {
            log.info("Initiating multipart upload: {}/{}", request.getBucketName(), request.getObjectKey());
            return uploadManagerStub.initiateMultipartUpload(request);
        } catch (StatusRuntimeException e) {
            log.error("Multipart upload initiation failed: {}", e.getStatus());
            throw new RuntimeException("Upload service unavailable: " + e.getStatus().getDescription());
        }
    }

    public UploadChunkResponseMessage uploadChunk(UploadChunkRequestMessage request) {
        try {
            log.info("Uploading chunk {} for upload: {}", request.getChunkIndex(), request.getUploadId());
            return uploadManagerStub.uploadChunk(request);
        } catch (StatusRuntimeException e) {
            log.error("Chunk upload failed: {}", e.getStatus());
            throw new RuntimeException("Upload service unavailable: " + e.getStatus().getDescription());
        }
    }

    public UploadResponseMessage completeMultipartUpload(CompleteMultipartRequestMessage request) {
        try {
            log.info("Completing multipart upload: {}", request.getUploadId());
            return uploadManagerStub.completeMultipartUpload(request);
        } catch (StatusRuntimeException e) {
            log.error("Multipart upload completion failed: {}", e.getStatus());
            throw new RuntimeException("Upload service unavailable: " + e.getStatus().getDescription());
        }
    }

    public AbortMultipartResponseMessage abortMultipartUpload(AbortMultipartRequestMessage request) {
        try {
            log.info("Aborting multipart upload: {}", request.getUploadId());
            return uploadManagerStub.abortMultipartUpload(request);
        } catch (StatusRuntimeException e) {
            log.error("Multipart upload abort failed: {}", e.getStatus());
            throw new RuntimeException("Upload service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== DOWNLOAD OPERATIONS ==========

    public DownloadResponseMessage retrieveFile(DownloadRequestMessage request) {
        try {
            log.info("Downloading file: {}/{}", request.getBucketName(), request.getObjectKey());
            return downloadManagerStub.retrieveFile(request);
        } catch (StatusRuntimeException e) {
            log.error("File download failed: {}", e.getStatus());
            throw new RuntimeException("Download service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== PRE-SIGNED URL OPERATIONS ==========

    public GeneratePreSignedUrlResponse generatePreSignedUrl(GeneratePreSignedUrlRequest request) {
        try {
            log.info("Generating pre-signed URL: {}/{} operation: {}",
                    request.getBucketName(), request.getObjectKey(), request.getOperation());
            return preSignedUrlStub.generatePreSignedUrl(request);
        } catch (StatusRuntimeException e) {
            log.error("Pre-signed URL generation failed: {}", e.getStatus());
            throw new RuntimeException("Pre-signed URL service unavailable: " + e.getStatus().getDescription());
        }
    }

    public ValidatePreSignedUrlResponse validatePreSignedUrl(ValidatePreSignedUrlRequest request) {
        try {
            log.info("Validating pre-signed URL for: {}/{}", request.getBucketName(), request.getObjectKey());
            return preSignedUrlStub.validatePreSignedUrl(request);
        } catch (StatusRuntimeException e) {
            log.error("Pre-signed URL validation failed: {}", e.getStatus());
            throw new RuntimeException("Pre-signed URL service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== CONVENIENCE METHODS FOR PRE-SIGNED URLS ==========

    /**
     * Convenience method to generate a download pre-signed URL
     */
    public GeneratePreSignedUrlResponse generateDownloadUrl(String bucketName, String objectKey,
                                                            String versionId, int expirationSeconds) {
        GeneratePreSignedUrlRequest request = GeneratePreSignedUrlRequest.newBuilder()
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .setVersionId(versionId != null ? versionId : "")
                .setOperation(Operation.DOWNLOAD)
                .setExpirationSeconds(expirationSeconds > 0 ? expirationSeconds : 3600) // Default 1 hour
                .build();

        return generatePreSignedUrl(request);
    }

    /**
     * Convenience method to generate an upload pre-signed URL
     */
    public GeneratePreSignedUrlResponse generateUploadUrl(String bucketName, String objectKey,
                                                          String contentType, int expirationSeconds) {
        GeneratePreSignedUrlRequest request = GeneratePreSignedUrlRequest.newBuilder()
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .setOperation(Operation.UPLOAD)
                .setContentType(contentType != null ? contentType : "application/octet-stream")
                .setExpirationSeconds(expirationSeconds > 0 ? expirationSeconds : 3600) // Default 1 hour
                .build();

        return generatePreSignedUrl(request);
    }

    /**
     * Convenience method to generate a delete pre-signed URL
     */
    public GeneratePreSignedUrlResponse generateDeleteUrl(String bucketName, String objectKey,
                                                          String versionId, int expirationSeconds) {
        GeneratePreSignedUrlRequest request = GeneratePreSignedUrlRequest.newBuilder()
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .setVersionId(versionId != null ? versionId : "")
                .setOperation(Operation.DELETE)
                .setExpirationSeconds(expirationSeconds > 0 ? expirationSeconds : 3600) // Default 1 hour
                .build();

        return generatePreSignedUrl(request);
    }

    /**
     * Quick validation method for pre-signed URLs
     */
    public boolean isValidPreSignedUrl(String url, String bucketName, String objectKey) {
        ValidatePreSignedUrlRequest request = ValidatePreSignedUrlRequest.newBuilder()
                .setUrl(url)
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .build();

        ValidatePreSignedUrlResponse response = validatePreSignedUrl(request);
        return response.getIsValid();
    }
}