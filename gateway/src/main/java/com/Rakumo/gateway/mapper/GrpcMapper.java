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

package com.rakumo.gateway.mapper;

import com.google.protobuf.Timestamp;
import com.rakumo.auth.grpc.AuthResponse;
import com.rakumo.auth.grpc.EmailVerificationRequest;
import com.rakumo.auth.grpc.JwtResponse;
import com.rakumo.auth.grpc.LoginRequest;
import com.rakumo.auth.grpc.LogoutRequest;
import com.rakumo.auth.grpc.MessageResponse;
import com.rakumo.auth.grpc.RefreshTokenRequest;
import com.rakumo.auth.grpc.RegisterRequest;
import com.rakumo.auth.grpc.TokenValidationResponse;
import com.rakumo.auth.grpc.UserResponse;
import com.rakumo.auth.grpc.ValidateTokenRequest;
import com.rakumo.gateway.dto.AuthDto;
import com.rakumo.gateway.dto.MetadataDto;
import com.rakumo.metadata.bucket.BucketListResponse;
import com.rakumo.metadata.bucket.BucketResponse;
import com.rakumo.metadata.bucket.CreateBucketRequest;
import com.rakumo.metadata.bucket.DeleteBucketRequest;
import com.rakumo.metadata.bucket.GetBucketRequest;
import com.rakumo.metadata.bucket.GetUserBucketsRequest;
import com.rakumo.metadata.bucket.ObjectMetadata;
import com.rakumo.metadata.bucket.UpdateBucketRequest;
import com.rakumo.metadata.object.version.DeleteResponse;
import com.rakumo.metadata.object.version.DeleteVersionRequest;
import com.rakumo.metadata.object.version.GetObjectVersionsRequest;
import com.rakumo.metadata.object.version.GetVersionRequest;
import com.rakumo.metadata.object.version.UpdateVersionRequest;
import com.rakumo.metadata.object.version.custom.CustomMetadata;
import com.rakumo.metadata.object.version.custom.CustomMetadataRequest;
import com.rakumo.metadata.object.version.custom.CustomMetadataResponse;
import com.rakumo.metadata.object.version.custom.GetMetadataRequest;
import com.rakumo.metadata.object.version.custom.RemoveMetadataRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between gRPC messages and Dtos used in the Gateway.
 * This class contains methods to map authentication requests/responses,
 * bucket requests/responses, version requests/responses, and custom metadata requests/responses.
 */
@Component
public class GrpcMapper {

  // ========== TIMESTAMP CONVERSIONS ==========

  private static Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }

  private static Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
  }

  // ========== AUTH MAPPINGS ==========

  /**
   * Converts a LoginRequestDto to a gRPC LoginRequest message.
   *
   * @param dto the LoginRequestDto containing the email and password for login
   * @return a LoginRequest gRPC message built from the provided LoginRequestDto
   */
  public LoginRequest toGrpcLogin(AuthDto.LoginRequestDto dto) {
    return LoginRequest.newBuilder()
            .setEmail(dto.email())
            .setPassword(dto.password())
            .build();
  }

  /**
   * Converts a RegisterRequestDto to a gRPC RegisterRequest message.
   *
   * @param dto the RegisterRequestDto containing the email, password, and username for registration
   * @return a RegisterRequest gRPC message built from the provided RegisterRequestDto
   */
  public RegisterRequest toGrpcRegister(AuthDto.RegisterRequestDto dto) {
    return RegisterRequest.newBuilder()
            .setEmail(dto.email())
            .setPassword(dto.password())
            .setUsername(dto.username())
            .build();
  }

  /**
   * Converts an EmailVerificationRequestDto to a gRPC EmailVerificationRequest message.
   *
   * @param dto the EmailVerificationRequestDto containing the email and OTP for email verification
   * @return an EmailVerificationRequest gRPC message
   */
  public EmailVerificationRequest toGrpcEmailVerify(AuthDto.EmailVerificationRequestDto dto) {
    return EmailVerificationRequest.newBuilder()
            .setEmail(dto.email())
            .setOtp(dto.otp())
            .build();
  }

  /**
   * Converts a RefreshTokenRequestDto to a gRPC RefreshTokenRequest message.
   *
   * @param dto the RefreshTokenRequestDto containing the refresh token for token refreshing
   * @return a RefreshTokenRequest gRPC message built from the provided RefreshTokenRequestDto
   */
  public RefreshTokenRequest toGrpcRefreshToken(AuthDto.RefreshTokenRequestDto dto) {
    return RefreshTokenRequest.newBuilder()
            .setRefreshToken(dto.refreshToken())
            .build();
  }

  /**
   * Converts a LogoutRequestDto to a gRPC LogoutRequest message.
   *
   * @param dto the LogoutRequestDto containing the refresh token for logout
   * @return a LogoutRequest gRPC message built from the provided LogoutRequestDto
   */
  public LogoutRequest toGrpcLogout(AuthDto.LogoutRequestDto dto) {
    return LogoutRequest.newBuilder()
            .setRefreshToken(dto.refreshToken())
            .build();
  }

  /**
   * Converts a ValidateTokenRequestDto to a gRPC ValidateTokenRequest message.
   *
   * @param dto the ValidateTokenRequestDto containing the token to be validated
   * @return a ValidateTokenRequest gRPC message built from the provided ValidateTokenRequestDto
   */
  public ValidateTokenRequest toGrpcValidateToken(AuthDto.ValidateTokenRequestDto dto) {
    return ValidateTokenRequest.newBuilder()
            .setToken(dto.token())
            .build();
  }

  /**
   * Converts a JwtResponse gRPC message to a JwtResponseDto.
   *
   * @param grpc the JwtResponse gRPC message containing the JWT response data
   * @return a JwtResponseDto built from the provided JwtResponse gRPC message
   */
  public AuthDto.JwtResponseDto toDtoJwt(JwtResponse grpc) {
    return new AuthDto.JwtResponseDto(
            grpc.getAccessToken(),
            grpc.getRefreshToken(),
            grpc.getExpiresIn(),
            grpc.getTokenType(),
            grpc.getUserId(),
            grpc.getEmail(),
            grpc.getRolesList()
    );
  }

  /**
   * Converts an AuthResponse gRPC message to an AuthResponseDto.
   *
   * @param grpc the AuthResponse gRPC message containing the authentication response data
   * @return an AuthResponseDto built from the provided AuthResponse gRPC message
   */
  public AuthDto.AuthResponseDto toDtoAuth(AuthResponse grpc) {
    AuthDto.AuthDataDto dataDto = null;
    if (grpc.hasData()) {
      dataDto = new AuthDto.AuthDataDto(
              grpc.getData().getQrCodeUrl(),
              grpc.getData().getSecretKey(),
              grpc.getData().getUserId(),
              grpc.getData().getEmail()
      );
    }
    return new AuthDto.AuthResponseDto(
            grpc.getSuccess(),
            grpc.getMessage(),
            dataDto
    );
  }

  /**
   * Converts a TokenValidationResponse gRPC message to a TokenValidationResponseDto.
   *
   * @param grpc the TokenValidationResponse gRPC message
   * @return a TokenValidationResponseDto built from the provided gRPC message
   */
  public AuthDto.TokenValidationResponseDto toDtoTokenValidation(TokenValidationResponse grpc) {
    return new AuthDto.TokenValidationResponseDto(
            grpc.getValid(),
            grpc.getUserId(),
            grpc.getEmail(),
            grpc.getRolesList(),
            grpc.getErrorMessage()
    );
  }

  /**
   * Converts a UserResponse gRPC message to a UserResponseDto.
   *
   * @param grpc the UserResponse gRPC message containing the user response data
   * @return a UserResponseDto built from the provided UserResponse gRPC message
   */
  public AuthDto.UserResponseDto toDtoUser(UserResponse grpc) {
    return new AuthDto.UserResponseDto(
            grpc.getId(),
            grpc.getEmail(),
            grpc.getUsername(),
            grpc.getIsEnabled(),
            grpc.getRolesList(),
            grpc.getCreatedAt(),
            grpc.getUpdatedAt()
    );
  }

  /**
   * Converts a MessageResponse gRPC message to a MessageResponseDto.
   *
   * @param grpc the MessageResponse gRPC message containing the message response data
   * @return a MessageResponseDto built from the provided MessageResponse gRPC message
   */
  public AuthDto.MessageResponseDto toDtoMessage(MessageResponse grpc) {
    return new AuthDto.MessageResponseDto(
            grpc.getSuccess(),
            grpc.getMessage()
    );
  }

  // ========== BUCKET MAPPINGS ==========

  /**
   * Converts a CreateBucketRequestDto to a gRPC CreateBucketRequest message.
   *
   * @param dto the CreateBucketRequestDto containing the bucket creation data
   * @return a CreateBucketRequest gRPC message built from the provided CreateBucketRequestDto
   */
  public CreateBucketRequest toGrpcCreateBucket(MetadataDto.CreateBucketRequestDto dto) {
    return CreateBucketRequest.newBuilder()
            .setOwnerId(dto.ownerId())
            .setName(dto.name())
            .setVersioningEnabled(dto.versioningEnabled())
            .setRegion(dto.region())
            .build();
  }

  /**
   * Converts a bucket ID and owner ID to a gRPC GetBucketRequest message.
   *
   * @param bucketId the ID of the bucket to be retrieved
   * @param ownerId the ID of the owner of the bucket
   * @return a GetBucketRequest gRPC message built from the provided bucket ID and owner ID
   */
  public GetBucketRequest toGrpcGetBucket(String bucketId, String ownerId) {
    return GetBucketRequest.newBuilder()
            .setBucketId(bucketId)
            .setOwnerId(ownerId)
            .build();
  }

  /**
   * Converts an UpdateBucketRequestDto to a gRPC UpdateBucketRequest message.
   *
   * @param dto the UpdateBucketRequestDto containing the bucket update data
   * @return an UpdateBucketRequest gRPC message built from the provided UpdateBucketRequestDto
   */
  public UpdateBucketRequest toGrpcUpdateBucket(MetadataDto.UpdateBucketRequestDto dto) {
    return UpdateBucketRequest.newBuilder()
            .setBucketId(dto.bucketId())
            .setOwnerId(dto.ownerId())
            .setName(dto.name())
            .setVersioningEnabled(dto.versioningEnabled())
            .setRegion(dto.region())
            .build();
  }

  /**
   * Converts a bucket ID and owner ID to a gRPC DeleteBucketRequest message.
   *
   * @param bucketId the ID of the bucket to be deleted
   * @param ownerId the ID of the owner of the bucket
   * @return a DeleteBucketRequest gRPC message built from the provided bucket ID and owner ID
   */
  public DeleteBucketRequest toGrpcDeleteBucket(String bucketId, String ownerId) {
    return DeleteBucketRequest.newBuilder()
            .setBucketId(bucketId)
            .setOwnerId(ownerId)
            .build();
  }

  /**
   * Converts an owner ID to a gRPC GetUserBucketsRequest message.
   *
   * @param ownerId the ID of the owner whose buckets are to be retrieved
   * @return a GetUserBucketsRequest gRPC message built from the provided owner ID
   */
  public GetUserBucketsRequest toGrpcGetUserBuckets(String ownerId) {
    return GetUserBucketsRequest.newBuilder()
            .setOwnerId(ownerId)
            .build();
  }

  /**
   * Converts a BucketResponse gRPC message to a BucketResponseDto.
   *
   * @param grpc the BucketResponse gRPC message containing the bucket response data
   * @return a BucketResponseDto built from the provided BucketResponse gRPC message
   */
  public MetadataDto.BucketResponseDto toDtoBucket(BucketResponse grpc) {
    return new MetadataDto.BucketResponseDto(
            grpc.getBucketId(),
            grpc.getOwnerId(),
            grpc.getName(),
            toInstant(grpc.getCreatedAt()).toString(), // Convert to String
            grpc.getVersioningEnabled(),
            grpc.getRegion(),
            grpc.getObjectsList().stream().map(GrpcMapper::toDtoObjectMetadata)
                    .collect(Collectors.toList()),
            toInstant(grpc.getUpdatedAt()).toString() // Convert to String
    );
  }

  /**
   * Converts a BucketListResponse gRPC message to a BucketListResponseDto.
   *
   * @param grpc the BucketListResponse gRPC message containing the bucket list response data
   * @return a BucketListResponseDto built from the provided BucketListResponse gRPC message
   */
  public MetadataDto.BucketListResponseDto toDtoBucketList(BucketListResponse grpc) {
    return new MetadataDto.BucketListResponseDto(
            grpc.getBucketsList().stream().map(this::toDtoBucket).collect(Collectors.toList())
    );
  }

  private static MetadataDto.ObjectMetadataDto toDtoObjectMetadata(ObjectMetadata grpc) {
    return new MetadataDto.ObjectMetadataDto(
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

  /**
   * Converts a DeleteResponse gRPC message to a DeleteResponseDto.
   *
   * @param grpc the DeleteResponse gRPC message containing the delete response data
   * @return a DeleteResponseDto built from the provided DeleteResponse gRPC message
   */
  public MetadataDto.DeleteResponseDto toDtoDelete(DeleteResponse grpc) {
    return new MetadataDto.DeleteResponseDto(
            grpc.getSuccess(),
            grpc.getMessage()
    );
  }

  // ========== VERSION MAPPINGS ==========

  /**
   * Converts a version ID and object ID to a gRPC GetVersionRequest message.
   *
   * @param versionId the ID of the version to be retrieved
   * @param objectId the ID of the object to which the version belongs
   * @return a GetVersionRequest gRPC message built from the provided version ID and object ID
   */
  public GetVersionRequest toGrpcGetVersion(String versionId, String objectId) {
    return GetVersionRequest.newBuilder()
            .setVersionId(versionId)
            .setObjectId(objectId)
            .build();
  }

  /**
   * Converts an object ID to a gRPC GetObjectVersionsRequest message.
   *
   * @param objectId the ID of the object whose versions are to be retrieved
   * @return a GetObjectVersionsRequest gRPC message built from the provided object ID
   */
  public GetObjectVersionsRequest toGrpcGetObjectVersions(String objectId) {
    return GetObjectVersionsRequest.newBuilder()
            .setObjectId(objectId)
            .build();
  }

  /**
   * Converts an UpdateVersionRequestDto to a gRPC UpdateVersionRequest message.
   *
   * @param dto the UpdateVersionRequestDto containing the version update data
   * @return an UpdateVersionRequest gRPC message built from the provided UpdateVersionRequestDto
   */
  public UpdateVersionRequest toGrpcUpdateVersion(MetadataDto.UpdateVersionRequestDto dto) {
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

  /**
   * Converts a version ID and object ID to a gRPC DeleteVersionRequest message.
   *
   * @param versionId the ID of the version to be deleted
   * @param objectId the ID of the object to which the version belongs
   * @return a DeleteVersionRequest gRPC message built from the provided version ID and object ID
   */
  public DeleteVersionRequest toGrpcDeleteVersion(String versionId, String objectId) {
    return DeleteVersionRequest.newBuilder()
            .setVersionId(versionId)
            .setObjectId(objectId)
            .build();
  }

  // ========== CUSTOM METADATA MAPPINGS ==========

  /**
   * Converts a CustomMetadataRequestDto to a gRPC CustomMetadataRequest message.
   *
   * @param dto the CustomMetadataRequestDto containing the custom metadata request data
   * @return a CustomMetadataRequest gRPC message built from the provided CustomMetadataRequestDto
   */
  public CustomMetadataRequest toGrpcCustomMetadataRequest(
          MetadataDto.CustomMetadataRequestDto dto) {
    return CustomMetadataRequest.newBuilder()
            .setVersionId(dto.versionId())
            .setKey(dto.key())
            .setValue(dto.value())
            .build();
  }

  /**
   * Converts a version ID to a gRPC GetMetadataRequest message.
   *
   * @param versionId the ID of the version whose custom metadata is to be retrieved
   * @return a GetMetadataRequest gRPC message built from the provided version ID
   */
  public GetMetadataRequest toGrpcGetMetadata(String versionId) {
    return GetMetadataRequest.newBuilder()
            .setVersionId(versionId)
            .build();
  }

  /**
   * Converts a version ID and key to a gRPC RemoveMetadataRequest message.
   *
   * @param versionId the ID of the version from which the custom metadata is to be removed
   * @param key the key of the custom metadata to be removed
   * @return a RemoveMetadataRequest gRPC message built from the provided version ID and key
   */
  public RemoveMetadataRequest toGrpcRemoveMetadata(String versionId, String key) {
    return RemoveMetadataRequest.newBuilder()
            .setVersionId(versionId)
            .setKey(key)
            .build();
  }

  /**
   * Converts a CustomMetadataResponse gRPC message to a CustomMetadataResponseDto.
   *
   * @param grpc the CustomMetadataResponse gRPC message containing the custom metadata response
   * @return a CustomMetadataResponseDto built from the provided CustomMetadataResponse gRPC message
   */
  public MetadataDto.CustomMetadataResponseDto toDtoCustomMetadataResponse(
          CustomMetadataResponse grpc) {
    return new MetadataDto.CustomMetadataResponseDto(
            grpc.getVersionId(),
            grpc.getMetadataList().stream().map(GrpcMapper::toDtoCustomMetadata)
                    .collect(Collectors.toList())
    );
  }

  private static CustomMetadata toGrpcCustomMetadata(MetadataDto.CustomMetadataDto dto) {
    return CustomMetadata.newBuilder()
            .setKey(dto.key())
            .setValue(dto.value())
            .build();
  }

  private static MetadataDto.CustomMetadataDto toDtoCustomMetadata(CustomMetadata grpc) {
    return new MetadataDto.CustomMetadataDto(
            grpc.getKey(),
            grpc.getValue()
    );
  }
}
