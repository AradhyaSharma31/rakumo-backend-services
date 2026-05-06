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

package com.rakumo.auth.service.impl;

import com.rakumo.auth.entity.RefreshToken;
import com.rakumo.auth.entity.User;
import com.rakumo.auth.exception.TokenRefreshException;
import com.rakumo.auth.repository.RefreshTokenRepository;
import com.rakumo.auth.service.RefreshTokenService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing refresh tokens.
 * This service handles creation, validation, and cleanup of refresh tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${app.jwt.refresh-token-expiration}")
  private Long refreshTokenDuration;

  @Override
  @Transactional
  public RefreshToken createRefreshToken(User user) {
    log.info("Creating refresh token for user: {}", user.getEmail());

    // Delete any existing refresh token for this user
    deleteByUser(user);

    // Create new refresh token
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDuration));
    refreshToken.setToken(UUID.randomUUID().toString()); // Using UUID as token

    RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
    log.debug("Refresh token created with ID: {}", savedToken.getId());

    return savedToken;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RefreshToken> findByToken(String token) {
    log.debug("Looking up refresh token");
    return refreshTokenRepository.findByToken(token);
  }

  @Override
  @Transactional
  public RefreshToken verifyExpiration(RefreshToken token) {
    log.debug("Verifying refresh token expiration for user: {}", token.getUser().getEmail());

    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      // Token expired, delete it
      refreshTokenRepository.delete(token);
      log.warn("Refresh token expired and deleted for user: {}", token.getUser().getEmail());
      throw TokenRefreshException.expired();
    }

    return token;
  }

  @Override
  @Transactional
  public void deleteByUser(User user) {
    log.debug("Deleting refresh tokens for user: {}", user.getEmail());
    refreshTokenRepository.deleteByUser(user);
  }

  @Override
  @Transactional
  public void deleteExpiredTokens() {
    log.info("Cleaning up expired refresh tokens");
    int deletedCount = refreshTokenRepository.deleteAllExpiredSince(Instant.now());
    log.info("Deleted {} expired refresh tokens", deletedCount);
  }

  /**
   * Checks if the user has a valid (non-expired) refresh token.
   *
   * @param user the user to check for a valid refresh token
   * @return true if a valid refresh token exists, false otherwise
   */
  @Transactional(readOnly = true)
  private boolean hasValidRefreshToken(User user) {
    return refreshTokenRepository.findByUser(user)
            .map(token -> !token.isExpired())
            .orElse(false);
  }

  /**
   * Revokes a refresh token by deleting it from the repository.
   *
   * @param token the token string to revoke
   */
  @Transactional
  private void revokeToken(String token) {
    refreshTokenRepository.findByToken(token)
            .ifPresent(refreshTokenRepository::delete);
    log.debug("Refresh token revoked");
  }
}
