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
import com.rakumo.auth.service.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of the TotpService interface using Google Authenticator library.
 */
@Slf4j
@Service
public class TotpServiceImpl implements TotpService {

  private final GoogleAuthenticator googleAuth;

  /**
   * Constructor initializes the GoogleAuthenticator instance.
   */
  public TotpServiceImpl() {
    this.googleAuth = new GoogleAuthenticator();
  }

  @Override
  public String generateSecretKey() {
    GoogleAuthenticatorKey key = googleAuth.createCredentials();
    String secretKey = key.getKey();
    log.info("Generated new TOTP secret key for user");
    return secretKey;
  }

  @Override
  public String getQrCodeUrl(User user, String secretKey) {
    String issuer = "rakumo";
    String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            issuer,
            user.getEmail(),
            new GoogleAuthenticatorKey.Builder(secretKey).build()
    );

    log.info("Generated QR code URL for user: {}", user.getEmail());
    return qrCodeUrl;
  }

  @Override
  public boolean validateCode(String secretKey, String code) {
    try {
      if (code == null || code.length() != 6) {
        log.warn("Invalid TOTP code format: {}", code);
        return false;
      }

      int verificationCode = Integer.parseInt(code);
      boolean isValid = googleAuth.authorize(secretKey, verificationCode);

      if (isValid) {
        log.info("TOTP code validation successful");
      } else {
        log.warn("TOTP code validation failed for code: {}", code);
      }

      return isValid;
    } catch (NumberFormatException e) {
      log.error("Invalid TOTP code format (not numeric): {}", code);
      return false;
    } catch (Exception e) {
      log.error("Error validating TOTP code: {}", e.getMessage());
      return false;
    }
  }
}
