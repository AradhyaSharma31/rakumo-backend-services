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

import com.rakumo.auth.entity.User;
import com.rakumo.auth.entity.VerificationToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing verification tokens.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

  /**
   * Finds a verification token by its token string.
   *
   * @param token the token string to search for
   * @return an Optional containing the found VerificationToken, or empty if not found
   */
  Optional<VerificationToken> findByToken(String token);

  /**
   * Finds a verification token associated with a specific user.
   *
   * @param user the User entity to search for
   * @return an Optional containing the found VerificationToken, or empty if not found
   */
  Optional<VerificationToken> findByUser(User user);

  /**
   * Finds a verification token associated with a specific user and token type.
   *
   * @param now the current time to check for token expiration
   * @return an Optional containing the found VerificationToken, or empty if not found
   */
  @Modifying
  @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < :now")
  int deleteAllExpiredSince(@Param("now") Instant now);

  /**
   * Deletes all verification tokens associated with a specific user.
   *
   * @param user the User entity whose tokens should be deleted
   */
  @Modifying
  @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user")
  void deleteByUser(@Param("user") User user);

  /**
   * Deletes all verification tokens associated with a specific user and token type.
   *
   * @param user the User entity whose tokens should be deleted
   * @param tokenType the type of tokens to delete
   */
  @Modifying
  @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user AND vt.tokenType = :tokenType")
  void deleteByUserAndTokenType(@Param("user") User user,
                                @Param("tokenType") VerificationToken.TokenType tokenType);
}
