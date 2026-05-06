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

package com.rakumo.gateway.controller;

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
import com.rakumo.gateway.mapper.GrpcMapper;
import com.rakumo.gateway.service.GrpcAuthClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling auth requests and forwarding them to the Auth microservice via gRPC.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthGatewayController {

  private final GrpcAuthClientService authClientService;
  private final GrpcMapper mapper;

  /**
   * Handles user registration requests by forwarding them to the Auth microservice via gRPC.
   *
   * @param requestDto the registration request data transfer object
   * @return a ResponseEntity containing the authentication response data transfer object
   */
  @PostMapping("/register")
  public ResponseEntity<AuthDto.AuthResponseDto> register(
          @RequestBody AuthDto.RegisterRequestDto requestDto) {
    RegisterRequest grpcRequest = mapper.toGrpcRegister(requestDto);
    AuthResponse grpcResponse = authClientService.register(grpcRequest);
    AuthDto.AuthResponseDto responseDto = mapper.toDtoAuth(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles user login requests by forwarding them to the Auth microservice via gRPC.
   *
   * @param requestDto the login request data transfer object
   * @return a ResponseEntity containing the JWT response data transfer object
   */
  @PostMapping("/login")
  public ResponseEntity<AuthDto.JwtResponseDto> login(
          @RequestBody AuthDto.LoginRequestDto requestDto) {
    LoginRequest grpcRequest = mapper.toGrpcLogin(requestDto);
    JwtResponse grpcResponse = authClientService.login(grpcRequest);
    AuthDto.JwtResponseDto responseDto = mapper.toDtoJwt(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles email verification requests by forwarding them to the Auth microservice via gRPC.
   *
   * @param requestDto the email verification request data transfer object
   * @return a ResponseEntity containing the authentication response data transfer object
   */
  @PostMapping("/verify-email")
  public ResponseEntity<AuthDto.AuthResponseDto> verifyEmail(
          @RequestBody AuthDto.EmailVerificationRequestDto requestDto) {
    EmailVerificationRequest grpcRequest = mapper.toGrpcEmailVerify(requestDto);
    AuthResponse grpcResponse = authClientService.verifyEmail(grpcRequest);
    AuthDto.AuthResponseDto responseDto = mapper.toDtoAuth(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles token refresh requests by forwarding them to the Auth microservice via gRPC.
   *
   * @param requestDto the refresh token request data transfer object
   * @return a ResponseEntity containing the JWT response data transfer object
   */
  @PostMapping("/refresh-token")
  public ResponseEntity<AuthDto.JwtResponseDto> refreshToken(
          @RequestBody AuthDto.RefreshTokenRequestDto requestDto) {
    RefreshTokenRequest grpcRequest = mapper.toGrpcRefreshToken(requestDto);
    JwtResponse grpcResponse = authClientService.refreshToken(grpcRequest);
    AuthDto.JwtResponseDto responseDto = mapper.toDtoJwt(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles user logout requests by forwarding them to the Auth microservice via gRPC.
   *
   * @param requestDto the logout request data transfer object
   * @return a ResponseEntity containing the message response data transfer object
   */
  @PostMapping("/logout")
  public ResponseEntity<AuthDto.MessageResponseDto> logout(
          @RequestBody AuthDto.LogoutRequestDto requestDto) {
    LogoutRequest grpcRequest = mapper.toGrpcLogout(requestDto);
    MessageResponse grpcResponse = authClientService.logout(grpcRequest);
    AuthDto.MessageResponseDto responseDto = mapper.toDtoMessage(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles token validation requests by forwarding them to the Auth microservice via gRPC.
   *
   * @param requestDto the token validation request data transfer object
   * @return a ResponseEntity containing the token validation response data transfer object
   */
  @PostMapping("/validate-token")
  public ResponseEntity<AuthDto.TokenValidationResponseDto> validateToken(
          @RequestBody AuthDto.ValidateTokenRequestDto requestDto) {
    ValidateTokenRequest grpcRequest = mapper.toGrpcValidateToken(requestDto);
    TokenValidationResponse grpcResponse = authClientService.validateToken(grpcRequest);
    AuthDto.TokenValidationResponseDto responseDto = mapper.toDtoTokenValidation(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles requests to get user information from a token via gRPC.
   *
   * @param requestDto the token validation request data transfer object
   * @return a ResponseEntity containing the user response data transfer object
   */
  @PostMapping("/user-from-token")
  public ResponseEntity<AuthDto.UserResponseDto> getUserFromToken(
          @RequestBody AuthDto.ValidateTokenRequestDto requestDto) {
    ValidateTokenRequest grpcRequest = mapper.toGrpcValidateToken(requestDto);
    UserResponse grpcResponse = authClientService.getUserFromToken(grpcRequest);
    AuthDto.UserResponseDto responseDto = mapper.toDtoUser(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles requests to check if a token is valid via gRPC.
   *
   * @param authHeader the Authorization header containing the token
   * @return a ResponseEntity containing a boolean indicating whether the token is valid
   */
  @GetMapping("/check-token")
  public ResponseEntity<Boolean> checkToken(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.ok(false);
    }
    String token = authHeader.substring(7);
    boolean isValid = authClientService.isValidToken(token);
    return ResponseEntity.ok(isValid);
  }
}
