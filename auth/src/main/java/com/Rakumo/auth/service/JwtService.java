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

import com.rakumo.auth.entity.User;

/**
 * Service interface for managing JWT tokens.
 */
public interface JwtService {

  /**
   * Generates a JWT access token for the given user.
   *
   * @param user the user for whom the access token is to be generated
   * @return a JWT access token as a String
   */
  String generateAccessToken(User user);

  /**
   * Generates a JWT refresh token for the given user.
   *
   * @param user the user for whom the refresh token is to be generated
   * @return a JWT refresh token as a String
   */
  String generateRefreshToken(User user);

  /**
   * Validates the given JWT token.
   *
   * @param token the JWT token to be validated
   * @return true if the token is valid, false otherwise
   */
  boolean validateToken(String token);

  /**
   * Extracts the username from the given JWT token.
   *
   * @param token the JWT token from which to extract the username
   * @return the username extracted from the token
   */
  String getUsernameFromToken(String token);

  /**
   * Extracts the user ID from the given JWT token.
   *
   * @param token the JWT token from which to extract the user ID
   * @return the user ID extracted from the token
   */
  String getUserIdFromToken(String token);

  /**
   * Checks if the given JWT token has expired.
   *
   * @param token the JWT token to check for expiration
   * @return true if the token has expired, false otherwise
   */
  boolean isTokenExpired(String token);
}
