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
import com.rakumo.auth.entity.VerificationToken;
import java.util.Optional;

/**
 * Service interface for managing verification tokens.
 */
public interface VerificationTokenService {

  /**
   * Creates a new verification token for the given user and secret key.
   *
   * @param user the user for whom the token is being created
   * @param secretKey the secret key associated with the token
   * @return the created VerificationToken
   */
  VerificationToken createVerificationToken(User user, String secretKey);

  /**
   * Finds a verification token by its token string.
   *
   * @param token the token string to search for
   * @return an Optional containing the found VerificationToken, or empty if not found
   */
  Optional<VerificationToken> findByToken(String token);

  /**
   * Verifies the given token string and returns the associated VerificationToken if valid.
   *
   * @param token the token string to verify
   * @return the associated VerificationToken if the token is valid, or null if invalid
   */
  VerificationToken verifyToken(String token);

  /**
   * Deletes the verification token associated with the given user.
   *
   * @param user the user whose verification token should be deleted
   */
  void deleteByUser(User user);

  /**
   * Deletes all expired verification tokens from the system.
   */
  void deleteExpiredTokens();

  /**
   * Retrieves the secret key associated with the given user.
   *
   * @param user the user whose secret key is being retrieved
   * @return an Optional containing the secret key if found, or empty if not found
   */
  Optional<String> getSecretKeyByUser(User user);

  /**
   * Finds a verification token by the associated user.
   *
   * @param user the user to search for
   * @return an Optional containing the found VerificationToken, or empty if not found
   */
  Optional<VerificationToken> findByUser(User user);
}
