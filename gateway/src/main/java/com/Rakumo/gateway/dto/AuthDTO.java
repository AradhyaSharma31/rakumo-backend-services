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

package com.rakumo.gateway.dto;

import java.util.List;

/**
 * Data Transfer Objects (Dtos) for authentication-related operations.
 */
public class AuthDto {

  /**
   * Dto for login request containing email and password.
   */
  public record LoginRequestDto(String email, String password) {}

  /**
   * Dto for user registration request containing email, password, and username.
   */
  public record RegisterRequestDto(String email, String password, String username) {}

  /**
   * Dto for email verification request containing email and OTP.
   */
  public record EmailVerificationRequestDto(String email, String otp) {}

  /**
   * Dto for refresh token request containing the refresh token.
   */
  public record RefreshTokenRequestDto(String refreshToken) {}

  /**
   * Dto for logout request containing the refresh token.
   */
  public record LogoutRequestDto(String refreshToken) {}

  /**
   * Dto for token validation request containing the token to be validated.
   */
  public record ValidateTokenRequestDto(String token) {}

  /**
   * Dto for authentication data containing QR code URL, secret key, user ID, and email.
   */
  public record AuthDataDto(String qrCodeUrl, String secretKey, String userId, String email) {}

  /**
   * Dto for authentication response containing success status, message, and authentication data.
   */
  public record AuthResponseDto(Boolean success, String message, AuthDataDto data) {}

  /**
   * Dto for JWT response containing
   * access token, refresh token, expiration time, token type, user ID, email, and roles.
   */
  public record JwtResponseDto(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String tokenType,
            String userId,
            String email,
            List<String> roles
  ) {}

  /**
   * Dto for token validation response containing
   * validity status, user ID, email, roles, and error message.
   */
  public record TokenValidationResponseDto(
            Boolean valid,
            String userId,
            String email,
            List<String> roles,
            String errorMessage
  ) {}

  /**
   * Dto for user response containing
   * user ID, email, username, enabled status, roles, and timestamps.
   */
  public record UserResponseDto(
            String id,
            String email,
            String username,
            Boolean isEnabled,
            List<String> roles,
            String createdAt,
            String updatedAt
  ) {}

  /**
   * Dto for message response containing success status and message.
   */
  public record MessageResponseDto(Boolean success, String message) {}

}
