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

import com.rakumo.auth.entity.User;
import com.rakumo.auth.entity.VerificationToken;
import com.rakumo.auth.exception.AuthException;
import com.rakumo.auth.repository.VerificationTokenRepository;
import com.rakumo.auth.service.VerificationTokenService;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing verification tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {

  private final VerificationTokenRepository verificationTokenRepository;

  @Value("${app.totp.verification-token-expiration:3600}") // 1 hour in seconds
  private Long verificationTokenDuration;

  @Override
  @Transactional
  public VerificationToken createVerificationToken(User user, String secretKey) {
    log.info("Creating verification token for user: {}", user.getEmail());

    // Delete any existing verification token for this user
    deleteByUser(user);

    // Create new verification token with TOTP secret
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setUser(user);
    verificationToken.setToken(secretKey); // Store the TOTP secret key
    verificationToken.setExpiryDate(Instant.now().plusSeconds(verificationTokenDuration));
    verificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);

    VerificationToken savedToken = verificationTokenRepository.save(verificationToken);
    log.debug("Verification token created with ID: {}", savedToken.getId());

    return savedToken;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VerificationToken> findByToken(String token) {
    log.debug("Looking up verification token");
    return verificationTokenRepository.findByToken(token);
  }

  @Override
  @Transactional
  public VerificationToken verifyToken(String token) {
    log.debug("Verifying token");

    VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new AuthException("Invalid verification token"));

    if (verificationToken.isExpired()) {
      verificationTokenRepository.delete(verificationToken);
      log.warn("Verification token expired for user: {}", verificationToken.getUser().getEmail());
      throw new AuthException("Verification token has expired");
    }

    return verificationToken;
  }

  @Override
  @Transactional
  public void deleteByUser(User user) {
    log.debug("Deleting verification tokens for user: {}", user.getEmail());
    verificationTokenRepository.deleteByUser(user);
  }

  @Override
  @Transactional
  public void deleteExpiredTokens() {
    log.info("Cleaning up expired verification tokens");
    int deletedCount = verificationTokenRepository.deleteAllExpiredSince(Instant.now());
    log.info("Deleted {} expired verification tokens", deletedCount);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> getSecretKeyByUser(User user) {
    log.debug("Getting TOTP secret key for user: {}", user.getEmail());
    return verificationTokenRepository.findByUser(user)
            .map(VerificationToken::getToken);
  }

  /**
   * Checks if the user has a valid (non-expired) verification token.
   *
   * @param user the user to check
   * @return true if a valid token exists, false otherwise
   */
  @Transactional(readOnly = true)
  private boolean hasValidVerificationToken(User user) {
    return verificationTokenRepository.findByUser(user)
            .map(token -> !token.isExpired())
            .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VerificationToken> findByUser(User user) {
    return verificationTokenRepository.findByUser(user);
  }

  /**
   * Extends the expiry date of the user's verification token by a specified number of seconds.
   *
   * @param user the user whose token expiry to extend
   * @param additionalSeconds the number of seconds to extend the token's expiry
   */
  @Transactional
  public void extendTokenExpiry(User user, int additionalSeconds) {
    verificationTokenRepository.findByUser(user)
            .ifPresent(token -> {
              token.setExpiryDate(Instant.now().plusSeconds(additionalSeconds));
              verificationTokenRepository.save(token);
              log.debug("Extended verification token expiry for user: {}", user.getEmail());
            });
  }
}
