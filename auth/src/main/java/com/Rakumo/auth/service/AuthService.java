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

package com.rakumo.auth.service;

import com.rakumo.auth.dto.reponse.AuthResponse;
import com.rakumo.auth.dto.reponse.JwtResponse;
import com.rakumo.auth.dto.request.EmailVerificationRequest;
import com.rakumo.auth.dto.request.LoginRequest;
import com.rakumo.auth.dto.request.RefreshTokenRequest;
import com.rakumo.auth.dto.request.RegisterRequest;
import org.apache.http.auth.InvalidCredentialsException;

/**
 * Service interface for handling authentication-related operations.
 */
public interface AuthService {

  /**
   * Registers a new user with the provided registration details.
   *
   * @param registerRequest the registration request containing user details
   * @return an AuthResponse containing information about the registered user
   */
  AuthResponse register(RegisterRequest registerRequest);

  /**
   * Authenticates a user with the provided login credentials.
   *
   * @param loginRequest the login request containing username and password
   * @return a JwtResponse containing the access token and refresh token
   * @throws InvalidCredentialsException if the provided credentials are invalid
   */
  JwtResponse login(LoginRequest loginRequest) throws InvalidCredentialsException;

  /**
   * Verifies a user's email address using the provided verification details.
   *
   * @param verificationRequest the email verification request containing the verification token
   * @return an AuthResponse indicating the result of the email verification
   */
  AuthResponse verifyEmail(EmailVerificationRequest verificationRequest);

  /**
   * Refreshes the access token using the provided refresh token.
   *
   * @param refreshTokenRequest the refresh token request containing the refresh token
   * @return a JwtResponse containing the new access token and refresh token
   */
  JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

  /**
   * Logs out a user by invalidating the provided refresh token.
   *
   * @param refreshToken the refresh token to be invalidated
   */
  void logout(String refreshToken);
}
