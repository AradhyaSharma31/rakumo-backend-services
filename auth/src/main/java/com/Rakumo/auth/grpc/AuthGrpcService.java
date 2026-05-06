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

package com.rakumo.auth.grpc;

import com.rakumo.auth.entity.User;
import com.rakumo.auth.exception.GlobalExceptionHandler;
import com.rakumo.auth.service.AuthService;
import com.rakumo.auth.service.JwtService;
import com.rakumo.auth.service.UserService;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC service implementation for authentication operations.
 * This class handles gRPC requests for user registration, login, email verification,
 * token refreshing, logout, token validation, and fetching user details from tokens.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

  private final AuthService authService;
  private final UserService userService;
  private final JwtService jwtService;
  private final GlobalExceptionHandler exceptionHandler;

  @Override
  public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
    try {
      log.info("gRPC Register request for email: {}", request.getEmail());

      com.rakumo.auth.dto.request.RegisterRequest registerRequest =
              new com.rakumo.auth.dto.request.RegisterRequest();
      registerRequest.setEmail(request.getEmail());
      registerRequest.setPassword(request.getPassword());
      registerRequest.setUsername(request.getUsername());

      com.rakumo.auth.dto.reponse.AuthResponse authResponse = authService.register(registerRequest);

      AuthResponse.Builder responseBuilder =
              AuthResponse.newBuilder()
                      .setSuccess(authResponse.isSuccess())
                      .setMessage(authResponse.getMessage());

      if (authResponse.getData() != null) {
        var data = authResponse.getData();
        if (data instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> dataMap = (Map<String, Object>) data;

          AuthData.Builder dataBuilder =
                  AuthData.newBuilder();

          if (dataMap.get("qrCodeUrl") != null) {
            dataBuilder.setQrCodeUrl(dataMap.get("qrCodeUrl").toString());
          }

          if (dataMap.get("secretKey") != null) {
            dataBuilder.setSecretKey(dataMap.get("secretKey").toString());
          }

          if (dataMap.get("userId") != null) {
            dataBuilder.setUserId(dataMap.get("userId").toString());
          }

          if (dataMap.get("email") != null) {
            dataBuilder.setEmail(dataMap.get("email").toString());
          }

          responseBuilder.setData(dataBuilder.build());
        }
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC register: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }

  @Override
  public void login(LoginRequest request, StreamObserver<JwtResponse> responseObserver) {
    try {
      log.info("gRPC Login request for email: {}", request.getEmail());

      com.rakumo.auth.dto.request.LoginRequest loginRequest =
            new com.rakumo.auth.dto.request.LoginRequest();
      loginRequest.setEmail(request.getEmail());
      loginRequest.setPassword(request.getPassword());

      com.rakumo.auth.dto.reponse.JwtResponse jwtResponse = authService.login(loginRequest);

      JwtResponse response =
              JwtResponse.newBuilder()
                      .setAccessToken(jwtResponse.getAccessToken())
                      .setRefreshToken(jwtResponse.getRefreshToken())
                      .setExpiresIn(jwtResponse.getExpiresIn())
                      .setTokenType(jwtResponse.getTokenType())
                      .setUserId(jwtResponse.getUserId())
                      .setEmail(jwtResponse.getEmail())
                      .addAllRoles(jwtResponse.getRoles())
                      .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC login: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }

  @Override
  public void verifyEmail(EmailVerificationRequest request,
                          StreamObserver<AuthResponse> responseObserver) {
    try {
      log.info("gRPC VerifyEmail request for email: {}", request.getEmail());

      com.rakumo.auth.dto.request.EmailVerificationRequest verificationRequest =
              new com.rakumo.auth.dto.request.EmailVerificationRequest();
      verificationRequest.setEmail(request.getEmail());
      verificationRequest.setOtp(request.getOtp());

      com.rakumo.auth.dto.reponse.AuthResponse authResponse =
              authService.verifyEmail(verificationRequest);

      AuthResponse response =
              AuthResponse.newBuilder()
                      .setSuccess(authResponse.isSuccess())
                      .setMessage(authResponse.getMessage())
                      .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC verifyEmail: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }

  @Override
  public void refreshToken(RefreshTokenRequest request,
                           StreamObserver<JwtResponse> responseObserver) {
    try {
      log.info("gRPC RefreshToken request");

      com.rakumo.auth.dto.request.RefreshTokenRequest refreshTokenRequest =
              new com.rakumo.auth.dto.request.RefreshTokenRequest();
      refreshTokenRequest.setRefreshToken(request.getRefreshToken());

      com.rakumo.auth.dto.reponse.JwtResponse jwtResponse =
              authService.refreshToken(refreshTokenRequest);

      JwtResponse response =
              JwtResponse.newBuilder()
                      .setAccessToken(jwtResponse.getAccessToken())
                      .setRefreshToken(jwtResponse.getRefreshToken())
                      .setExpiresIn(jwtResponse.getExpiresIn())
                      .setTokenType(jwtResponse.getTokenType())
                      .setUserId(jwtResponse.getUserId())
                      .setEmail(jwtResponse.getEmail())
                      .addAllRoles(jwtResponse.getRoles())
                      .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC refreshToken: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }

  @Override
  public void logout(LogoutRequest request, StreamObserver<MessageResponse> responseObserver) {
    try {
      log.info("gRPC Logout request");

      authService.logout(request.getRefreshToken());

      MessageResponse response =
              MessageResponse.newBuilder()
                      .setSuccess(true)
                      .setMessage("Logout successful")
                      .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC logout: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }

  @Override
  public void validateToken(ValidateTokenRequest request,
                            StreamObserver<TokenValidationResponse> responseObserver) {
    try {
      log.info("gRPC ValidateToken request");

      String token = request.getToken();
      boolean isValid = jwtService.validateToken(token);

      TokenValidationResponse.Builder responseBuilder =
              TokenValidationResponse.newBuilder()
                      .setValid(isValid);

      if (isValid) {
        String userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getUsernameFromToken(token);

        // Get user to fetch roles
        User user = userService.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        responseBuilder
                .setUserId(userId)
                .setEmail(email)
                .addAllRoles(user.getRoles());
      } else {
        responseBuilder.setErrorMessage("Invalid token");
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC validateToken: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }

  @Override
  public void getUserFromToken(ValidateTokenRequest request,
                               StreamObserver<UserResponse> responseObserver) {
    try {
      log.info("gRPC GetUserFromToken request");

      String token = request.getToken();

      if (!jwtService.validateToken(token)) {
        log.error("Invalid token");
        return;
      }

      String userId = jwtService.getUserIdFromToken(token);
      User user = userService.findById(UUID.fromString(userId))
              .orElseThrow(() -> new RuntimeException("User not found"));

      UserResponse response =
              UserResponse.newBuilder()
                      .setId(user.getId().toString())
                      .setEmail(user.getEmail())
                      .setUsername(user.getUsername() != null
                              ? user.getUsername()
                              : "")
                      .setIsEnabled(user.getIsEnabled())
                      .addAllRoles(user.getRoles())
                      .setCreatedAt(user.getCreatedAt().toString())
                      .setUpdatedAt(user.getUpdatedAt() != null
                              ? user.getUpdatedAt().toString()
                              : "")
                      .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error in gRPC getUserFromToken: {}", e.getMessage());
      responseObserver.onError(exceptionHandler.handleException(e));
    }
  }
}
