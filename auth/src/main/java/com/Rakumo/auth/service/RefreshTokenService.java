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

import com.rakumo.auth.entity.RefreshToken;
import com.rakumo.auth.entity.User;
import java.util.Optional;

/**
 * Service interface for managing refresh tokens.
 */
public interface RefreshTokenService {

  /**
   * Creates a new refresh token for the specified user.
   *
   * @param user the User for whom the refresh token is to be created
   * @return the created RefreshToken
   */
  RefreshToken createRefreshToken(User user);

  /**
   * Finds a refresh token by its token string.
   *
   * @param token the token string to search for
   * @return an Optional containing the found RefreshToken, or empty if not found
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Verifies if the provided refresh token has expired and is still valid.
   *
   * @param token the RefreshToken to verify
   * @return the same RefreshToken if it is valid and not expired
   * @throws RuntimeException if the token has expired or is invalid
   */
  RefreshToken verifyExpiration(RefreshToken token);

  /**
   * Deletes all refresh tokens associated with the specified user.
   *
   * @param user the User whose refresh tokens are to be deleted
   */
  void deleteByUser(User user);

  /**
   * Deletes all expired refresh tokens from the database.
   */
  void deleteExpiredTokens();
}
