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

package com.rakumo.auth.repository;

import com.rakumo.auth.entity.RefreshToken;
import com.rakumo.auth.entity.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing refresh tokens in the authentication service.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  /**
   * Finds a refresh token by its token string.
   *
   * @param token the token string to search for
   * @return an Optional containing the found RefreshToken, or empty if not found
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Finds a refresh token associated with the specified user.
   *
   * @param user the user whose refresh token is to be found
   * @return an Optional containing the found RefreshToken, or empty if not found
   */
  Optional<RefreshToken> findByUser(User user);

  /**
   * Deletes all refresh tokens that have expired before the specified time.
   *
   * @param now the current time used to determine which tokens are expired
   * @return the number of tokens that were deleted
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
  int deleteAllExpiredSince(@Param("now") Instant now);

  /**
   * Deletes the refresh token associated with the specified user.
   *
   * @param user the user whose refresh token should be deleted
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
  void deleteByUser(@Param("user") User user);

  /**
   * Checks if a refresh token exists for the specified user.
   *
   * @param user the user to check for an associated refresh token
   * @return true if a refresh token exists for the user, false otherwise
   */
  Boolean existsByUser(User user);
}
