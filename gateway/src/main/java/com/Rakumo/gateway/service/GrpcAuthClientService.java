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

package com.rakumo.gateway.service;

import com.rakumo.auth.grpc.AuthResponse;
import com.rakumo.auth.grpc.AuthServiceGrpc;
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
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * Service for communicating with the Auth service via gRPC.
 * This service provides methods to call the Auth service's gRPC endpoints
 * for user registration, login, email verification, token refresh, logout,
 * token validation, and retrieving user information from tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcAuthClientService {

  @GrpcClient("auth-service")
  private AuthServiceGrpc.AuthServiceBlockingStub authStub;

  /**
   * Calls the Auth service's register endpoint via gRPC.
   *
   * @param request the registration request containing user details
   * @return the authentication response from the Auth service
   */
  public AuthResponse register(RegisterRequest request) {
    try {
      log.info("Calling Auth service register for: {}", request.getEmail());
      return authStub.register(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC register call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Calls the Auth service's login endpoint via gRPC.
   *
   * @param request the login request containing email and password
   * @return the JWT response containing access and refresh tokens
   */
  public JwtResponse login(LoginRequest request) {
    try {
      log.info("Calling Auth service login for: {}", request.getEmail());
      return authStub.login(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC login call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Calls the Auth service's verifyEmail endpoint via gRPC.
   *
   * @param request the email verification request containing email and OTP
   * @return the authentication response indicating success or failure of verification
   */
  public AuthResponse verifyEmail(EmailVerificationRequest request) {
    try {
      log.info("Calling Auth service verifyEmail for: {}", request.getEmail());
      return authStub.verifyEmail(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC verifyEmail call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Calls the Auth service's refreshToken endpoint via gRPC.
   *
   * @param request the refresh token request containing the refresh token
   * @return the JWT response containing new access and refresh tokens
   */
  public JwtResponse refreshToken(RefreshTokenRequest request) {
    try {
      log.info("Calling Auth service refreshToken");
      return authStub.refreshToken(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC refreshToken call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Calls the Auth service's logout endpoint via gRPC.
   *
   * @param request the logout request containing the refresh token to invalidate
   * @return a message response indicating success or failure of logout
   */
  public MessageResponse logout(LogoutRequest request) {
    try {
      log.info("Calling Auth service logout");
      return authStub.logout(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC logout call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Calls the Auth service's validateToken endpoint via gRPC.
   *
   * @param request the token validation request containing the token to validate
   * @return the token validation response indicating whether the token is valid
   */
  public TokenValidationResponse validateToken(ValidateTokenRequest request) {
    try {
      log.info("Calling Auth service validateToken");
      return authStub.validateToken(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC validateToken call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Calls the Auth service's getUserFromToken endpoint via gRPC.
   *
   * @param request the request containing the token to extract user information from
   * @return the user response containing user details associated with the token
   */
  public UserResponse getUserFromToken(ValidateTokenRequest request) {
    try {
      log.info("Calling Auth service getUserFromToken");
      return authStub.getUserFromToken(request);
    } catch (StatusRuntimeException e) {
      log.error("gRPC getUserFromToken call failed: {}", e.getStatus());
      throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * method to check if a token is valid by calling the validateToken gRPC method.
   *
   * @param token the JWT token to validate
   * @return true if the token is valid, false otherwise
   */
  public boolean isValidToken(String token) {
    try {
      ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
              .setToken(token)
              .build();
      TokenValidationResponse response = validateToken(request);
      return response.getValid();
    } catch (Exception e) {
      log.error("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * method to retrieve user information from a token by calling the getUserFromToken gRPC method.
   *
   * @param token the JWT token to extract user information from
   * @return the user response containing user details associated with the token
   */
  public UserResponse getUserInfo(String token) {
    ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
            .setToken(token)
            .build();
    return getUserFromToken(request);
  }
}
