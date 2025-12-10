package com.Rakumo.gateway.mapper;

import com.Rakumo.gateway.dto.AuthDTO.*;
import com.Rakumo.gateway.dto.MetadataDTO.*;
import com.Rakumo.gateway.dto.ObjectDTO.*;
import com.Rakumo.auth.grpc.*;
import com.Rakumo.metadata.bucket.*;
import com.Rakumo.metadata.bucket.DeleteResponse;
import com.Rakumo.metadata.object.*;
import com.Rakumo.metadata.object.version.*;
import com.Rakumo.metadata.object.version.custom.CustomMetadata;
import com.Rakumo.metadata.object.version.custom.CustomMetadataRequest;
import com.Rakumo.metadata.object.version.custom.CustomMetadataResponse;
import com.Rakumo.metadata.object.version.custom.GetMetadataRequest;
import com.Rakumo.metadata.object.version.custom.RemoveMetadataRequest;
import com.Rakumo.object.download.*;
import com.Rakumo.object.presigned.*;
import com.Rakumo.object.storage.*;
import com.Rakumo.object.upload.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class GrpcMapper {

    // ========== TIMESTAMP CONVERSIONS ==========

    private Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    // ========== AUTH MAPPINGS ==========

    public LoginRequest toGrpcLogin(LoginRequestDTO dto) {
        return LoginRequest.newBuilder()
                .setEmail(dto.email())
                .setPassword(dto.password())
                .build();
    }

    public RegisterRequest toGrpcRegister(RegisterRequestDTO dto) {
        return RegisterRequest.newBuilder()
                .setEmail(dto.email())
                .setPassword(dto.password())
                .setUsername(dto.username())
                .build();
    }

    public EmailVerificationRequest toGrpcEmailVerify(EmailVerificationRequestDTO dto) {
        return EmailVerificationRequest.newBuilder()
                .setEmail(dto.email())
                .setOtp(dto.otp())
                .build();
    }

    public RefreshTokenRequest toGrpcRefreshToken(RefreshTokenRequestDTO dto) {
        return RefreshTokenRequest.newBuilder()
                .setRefreshToken(dto.refreshToken())
                .build();
    }

    public LogoutRequest toGrpcLogout(LogoutRequestDTO dto) {
        return LogoutRequest.newBuilder()
                .setRefreshToken(dto.refreshToken())
                .build();
    }

    public ValidateTokenRequest toGrpcValidateToken(ValidateTokenRequestDTO dto) {
        return ValidateTokenRequest.newBuilder()
                .setToken(dto.token())
                .build();
    }

    public JwtResponseDTO toDtoJwt(JwtResponse grpc) {
        return new JwtResponseDTO(
                grpc.getAccessToken(),
                grpc.getRefreshToken(),
                grpc.getExpiresIn(),
                grpc.getTokenType(),
                grpc.getUserId(),
                grpc.getEmail(),
                grpc.getRolesList()
        );
    }

    public AuthResponseDTO toDtoAuth(AuthResponse grpc) {
        AuthDataDTO dataDto = null;
        if (grpc.hasData()) {
            dataDto = new AuthDataDTO(
                    grpc.getData().getQrCodeUrl(),
                    grpc.getData().getSecretKey(),
                    grpc.getData().getUserId(),
                    grpc.getData().getEmail()
            );
        }
        return new AuthResponseDTO(
                grpc.getSuccess(),
                grpc.getMessage(),
                dataDto
        );
    }

    public TokenValidationResponseDTO toDtoTokenValidation(TokenValidationResponse grpc) {
        return new TokenValidationResponseDTO(
                grpc.getValid(),
                grpc.getUserId(),
                grpc.getEmail(),
                grpc.getRolesList(),
                grpc.getErrorMessage()
        );
    }

    public UserResponseDTO toDtoUser(UserResponse grpc) {
        return new UserResponseDTO(
                grpc.getId(),
                grpc.getEmail(),
                grpc.getUsername(),
                grpc.getIsEnabled(),
                grpc.getRolesList(),
                grpc.getCreatedAt(),
                grpc.getUpdatedAt()
        );
    }

    public MessageResponseDTO toDtoMessage(MessageResponse grpc) {
        return new MessageResponseDTO(
                grpc.getSuccess(),
                grpc.getMessage()
        );
    }

    // ========== BUCKET MAPPINGS ==========

    public CreateBucketRequest toGrpcCreateBucket(CreateBucketRequestDTO dto) {
        return CreateBucketRequest.newBuilder()
                .setOwnerId(dto.ownerId())
                .setName(dto.name())
                .setVersioningEnabled(dto.versioningEnabled())
                .setRegion(dto.region())
                .build();
    }

    public GetBucketRequest toGrpcGetBucket(String bucketId, String ownerId) {
        return GetBucketRequest.newBuilder()
                .setBucketId(bucketId)
                .setOwnerId(ownerId)
                .build();
    }

    public UpdateBucketRequest toGrpcUpdateBucket(UpdateBucketRequestDTO dto) {
        return UpdateBucketRequest.newBuilder()
                .setBucketId(dto.bucketId())
                .setOwnerId(dto.ownerId())
                .setName(dto.name())
                .setVersioningEnabled(dto.versioningEnabled())
                .setRegion(dto.region())
                .build();
    }

    public DeleteBucketRequest toGrpcDeleteBucket(String bucketId, String ownerId) {
        return DeleteBucketRequest.newBuilder()
                .setBucketId(bucketId)
                .setOwnerId(ownerId)
                .build();
    }

    public GetUserBucketsRequest toGrpcGetUserBuckets(String ownerId) {
        return GetUserBucketsRequest.newBuilder()
                .setOwnerId(ownerId)
                .build();
    }

    public BucketResponseDTO toDtoBucket(BucketResponse grpc) {
        return new BucketResponseDTO(
                grpc.getBucketId(),
                grpc.getOwnerId(),
                grpc.getName(),
                toInstant(grpc.getCreatedAt()).toString(), // Convert to String
                grpc.getVersioningEnabled(),
                grpc.getRegion(),
                grpc.getObjectsList().stream().map(this::toDtoObjectMetadata).collect(Collectors.toList()),
                toInstant(grpc.getUpdatedAt()).toString() // Convert to String
        );
    }

    public BucketListResponseDTO toDtoBucketList(BucketListResponse grpc) {
        return new BucketListResponseDTO(
                grpc.getBucketsList().stream().map(this::toDtoBucket).collect(Collectors.toList())
        );
    }

    private ObjectMetadataDTO toDtoObjectMetadata(ObjectMetadata grpc) {
        return new ObjectMetadataDTO(
                grpc.getId(),
                grpc.getBucketId(),
                toInstant(grpc.getCreatedAt()).toString(), // Convert to String
                grpc.getObjectKey(),
                grpc.getLatestVersionId(),
                grpc.getLatestEtag(),
                grpc.getLatestSize(),
                toInstant(grpc.getUpdatedAt()).toString(), // Convert to String
                grpc.getIsDeleted()
        );
    }

    public DeleteResponseDTO toDtoDelete(DeleteResponse grpc) {
        return new DeleteResponseDTO(
                grpc.getSuccess(),
                grpc.getMessage()
        );
    }

    // ========== OBJECT MAPPINGS ==========

    public CreateObjectRequest toGrpcCreateObject(CreateObjectRequestDTO dto) {
        return CreateObjectRequest.newBuilder()
                .setBucketId(dto.bucketId())
                .setObjectKey(dto.objectKey())
                .setLatestVersionId(dto.latestVersionId())
                .setLatestEtag(dto.latestEtag())
                .setLatestSize(dto.latestSize())
                .build();
    }

    public GetObjectRequest toGrpcGetObject(String objectId, String bucketId) {
        return GetObjectRequest.newBuilder()
                .setObjectId(objectId)
                .setBucketId(bucketId)
                .build();
    }

    public GetBucketObjectsRequest toGrpcGetBucketObjects(String bucketId) {
        return GetBucketObjectsRequest.newBuilder()
                .setBucketId(bucketId)
                .build();
    }

    public UpdateObjectRequest toGrpcUpdateObject(UpdateObjectRequestDTO dto) {
        return UpdateObjectRequest.newBuilder()
                .setObjectId(dto.objectId())
                .setBucketId(dto.bucketId())
                .setLatestVersionId(dto.latestVersionId())
                .setLatestEtag(dto.latestEtag())
                .setLatestSize(dto.latestSize())
                .setIsDeleted(dto.isDeleted())
                .build();
    }

    public DeleteObjectRequest toGrpcDeleteObject(String objectId, String bucketId) {
        return DeleteObjectRequest.newBuilder()
                .setObjectId(objectId)
                .setBucketId(bucketId)
                .build();
    }

    public ObjectResponseDTO toDtoObject(ObjectResponse grpc) {
        return new ObjectResponseDTO(
                grpc.getId(),
                grpc.getBucketId(),
                grpc.getObjectKey(),
                grpc.getLatestVersionId(),
                grpc.getLatestEtag(),
                grpc.getLatestSize(),
                toInstant(grpc.getCreatedAt()).toString(), // Convert to String
                toInstant(grpc.getUpdatedAt()).toString(), // Convert to String
                grpc.getIsDeleted(),
                grpc.getVersionsList().stream().map(this::toDtoObjectVersion).collect(Collectors.toList())
        );
    }

    public ObjectListResponseDTO toDtoObjectList(ObjectListResponse grpc) {
        return new ObjectListResponseDTO(
                grpc.getObjectsList().stream().map(this::toDtoObject).collect(Collectors.toList())
        );
    }

    private ObjectVersionDTO toDtoObjectVersion(ObjectVersion grpc) {
        return new ObjectVersionDTO(
                grpc.getVersionId(),
                grpc.getObjectId(),
                grpc.getEtag(),
                grpc.getSize(),
                toInstant(grpc.getCreatedAt()).toString(), // Convert to String
                grpc.getIsLatest()
        );
    }

    // ========== VERSION MAPPINGS ==========

    public GetVersionRequest toGrpcGetVersion(String versionId, String objectId) {
        return GetVersionRequest.newBuilder()
                .setVersionId(versionId)
                .setObjectId(objectId)
                .build();
    }

    public GetObjectVersionsRequest toGrpcGetObjectVersions(String objectId) {
        return GetObjectVersionsRequest.newBuilder()
                .setObjectId(objectId)
                .build();
    }

    public UpdateVersionRequest toGrpcUpdateVersion(UpdateVersionRequestDTO dto) {
        return UpdateVersionRequest.newBuilder()
                .setVersionId(dto.versionId())
                .setObjectId(dto.objectId())
                .setEtag(dto.etag())
                .setStorageLocation(dto.storageLocation())
                .setSize(dto.size())
                .setContentType(dto.contentType())
                .setIsDeleteMarker(dto.isDeleteMarker())
                .setStorageClass(dto.storageClass())
                .build();
    }

    public DeleteVersionRequest toGrpcDeleteVersion(String versionId, String objectId) {
        return DeleteVersionRequest.newBuilder()
                .setVersionId(versionId)
                .setObjectId(objectId)
                .build();
    }

    // ========== CUSTOM METADATA MAPPINGS ==========

    public CustomMetadataRequest toGrpcCustomMetadataRequest(CustomMetadataRequestDTO dto) {
        return CustomMetadataRequest.newBuilder()
                .setVersionId(dto.versionId())
                .setKey(dto.key())
                .setValue(dto.value())
                .build();
    }

    public GetMetadataRequest toGrpcGetMetadata(String versionId) {
        return GetMetadataRequest.newBuilder()
                .setVersionId(versionId)
                .build();
    }

    public RemoveMetadataRequest toGrpcRemoveMetadata(String versionId, String key) {
        return RemoveMetadataRequest.newBuilder()
                .setVersionId(versionId)
                .setKey(key)
                .build();
    }

    public CustomMetadataResponseDTO toDtoCustomMetadataResponse(CustomMetadataResponse grpc) {
        return new CustomMetadataResponseDTO(
                grpc.getVersionId(),
                grpc.getMetadataList().stream().map(this::toDtoCustomMetadata).collect(Collectors.toList())
        );
    }

    private CustomMetadata toGrpcCustomMetadata(CustomMetadataDTO dto) {
        return CustomMetadata.newBuilder()
                .setKey(dto.key())
                .setValue(dto.value())
                .build();
    }

    private CustomMetadataDTO toDtoCustomMetadata(CustomMetadata grpc) {
        return new CustomMetadataDTO(
                grpc.getKey(),
                grpc.getValue()
        );
    }

    // ========== FILE STORAGE MAPPINGS ==========

    public StoreFileRequestMessage toGrpcStoreFile(StoreFileRequestDTO dto) {
        return StoreFileRequestMessage.newBuilder()
                .setOwnerId(dto.ownerId())
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setFileData(ByteString.copyFrom(dto.fileData()))
                .setContentType(dto.contentType())
                .setExpectedChecksum(dto.expectedChecksum())
                .build();
    }

    public RetrieveFileRequestMessage toGrpcRetrieveFile(RetrieveFileRequestDTO dto) {
        return RetrieveFileRequestMessage.newBuilder()
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setVersionId(dto.versionId())
                .build();
    }

    public DeleteFileRequestMessage toGrpcDeleteFile(DeleteFileRequestDTO dto) {
        return DeleteFileRequestMessage.newBuilder()
                .setOwnerId(dto.ownerId())
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setFileId(dto.fileId())
                .build();
    }

    public StoreFileResponseDTO toDtoStoreFile(StoreFileResponseMessage grpc) {
        return new StoreFileResponseDTO(
                grpc.getBucketName(),
                grpc.getObjectKey(),
                grpc.getVersionId(),
                grpc.getChecksum(),
                grpc.getSizeBytes()
        );
    }

    public DeleteFileResponseDTO toDtoDeleteFile(DeleteFileResponseMessage grpc) {
        return new DeleteFileResponseDTO(
                grpc.getSuccess(),
                grpc.getMessage()
        );
    }

     // ========== UPLOAD MANAGER MAPPINGS ==========

    public UploadFileRequestMessage toGrpcUploadFile(UploadFileRequestDTO dto) {
        return UploadFileRequestMessage.newBuilder()
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setOwnerId(dto.ownerId())
                .setContentType(dto.contentType())
                .setFileData(ByteString.copyFrom(dto.fileData()))
                .build();
    }

    public InitiateMultipartRequestMessage toGrpcInitiateMultipart(InitiateMultipartRequestDTO dto) {
        return InitiateMultipartRequestMessage.newBuilder()
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setOwnerId(dto.ownerId())
                .setContentType(dto.contentType())
                .build();
    }

    public UploadChunkRequestMessage toGrpcUploadChunk(UploadChunkRequestDTO dto) {
        return UploadChunkRequestMessage.newBuilder()
                .setUploadId(dto.uploadId())
                .setChunkIndex(dto.chunkIndex())
                .setChunkData(ByteString.copyFrom(dto.chunkData()))
                .build();
    }

    public CompleteMultipartRequestMessage toGrpcCompleteMultipart(CompleteMultipartRequestDTO dto) {
        return CompleteMultipartRequestMessage.newBuilder()
                .setUploadId(dto.uploadId())
                .build();
    }

    public AbortMultipartRequestMessage toGrpcAbortMultipart(AbortMultipartRequestDTO dto) {
        return AbortMultipartRequestMessage.newBuilder()
                .setUploadId(dto.uploadId())
                .build();
    }

    public InitiateMultipartResponseDTO toDtoInitiateMultipart(InitiateMultipartResponseMessage grpc) {
        return new InitiateMultipartResponseDTO(
                grpc.getUploadId()
        );
    }

    public UploadChunkResponseDTO toDtoUploadChunk(UploadChunkResponseMessage grpc) {
        return new UploadChunkResponseDTO(
                grpc.getSuccess(),
                grpc.getChunkIndex(),
                grpc.getUploadId()
        );
    }

    public UploadResponseDTO toDtoUpload(UploadResponseMessage grpc) {
        return new UploadResponseDTO(
                grpc.getBucketName(),
                grpc.getObjectKey(),
                grpc.getVersionId(),
                grpc.getChecksum(),
                grpc.getSizeBytes(),
                toInstant(grpc.getUploadedAt()).toString() // Convert to String
        );
    }

    public AbortMultipartResponseDTO toDtoAbortMultipart(AbortMultipartResponseMessage grpc) {
        return new AbortMultipartResponseDTO(
                grpc.getSuccess(),
                grpc.getUploadId()
        );
    }

    // ========== DOWNLOAD MANAGER MAPPINGS ==========

    public DownloadRequestMessage toGrpcDownload(DownloadRequestDTO dto) {
        return DownloadRequestMessage.newBuilder()
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setVersionId(dto.versionId())
                .build();
    }

    public DownloadResponseDTO toDtoDownload(DownloadResponseMessage grpc) {
        return new DownloadResponseDTO(
                grpc.getBucketName(),
                grpc.getObjectKey(),
                grpc.getVersionId(),
                grpc.getContentLength(),
                grpc.getContentType(),
                grpc.getChecksum(),
                toInstant(grpc.getLastModified()).toString(), // Convert to String
                grpc.getFileData().toByteArray()
        );
    }

    public DeleteResponseDTO toDtoDelete(com.Rakumo.metadata.object.DeleteResponse grpc) {
        return new DeleteResponseDTO(
                grpc.getSuccess(),
                grpc.getMessage()
        );
    }

    // Pre-signed URL mappings
    public GeneratePreSignedUrlRequest toGrpcGeneratePreSignedUrl(GeneratePreSignedUrlRequestDTO dto) {
        Operation operation = Operation.valueOf(dto.operation());

        return GeneratePreSignedUrlRequest.newBuilder()
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .setVersionId(dto.versionId() != null ? dto.versionId() : "")
                .setOperation(operation)
                .setExpirationSeconds(dto.expirationSeconds() != null ? dto.expirationSeconds() : 3600)
                .setContentType(dto.contentType() != null ? dto.contentType() : "")
                .build();
    }

    public GeneratePreSignedUrlResponseDTO toDtoGeneratePreSignedUrl(GeneratePreSignedUrlResponse grpc) {
        Instant expirationInstant = Instant.ofEpochSecond(grpc.getExpiration().getSeconds(), grpc.getExpiration().getNanos());
        String expirationString = expirationInstant.toString();

        return new GeneratePreSignedUrlResponseDTO(
                grpc.getPreSignedUrl(),
                grpc.getBucketName(),
                grpc.getObjectKey(),
                grpc.getVersionId(),
                grpc.getOperation().name(),
                expirationString
        );
    }

    public ValidatePreSignedUrlRequest toGrpcValidatePreSignedUrl(ValidatePreSignedUrlRequestDTO dto) {
        return ValidatePreSignedUrlRequest.newBuilder()
                .setUrl(dto.url())
                .setBucketName(dto.bucketName())
                .setObjectKey(dto.objectKey())
                .build();
    }

    public ValidatePreSignedUrlResponseDTO toDtoValidatePreSignedUrl(ValidatePreSignedUrlResponse grpc) {
        return new ValidatePreSignedUrlResponseDTO(grpc.getIsValid());
    }
}