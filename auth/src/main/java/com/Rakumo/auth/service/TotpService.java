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
 * Service interface for handling Time-based One-Time Password (TOTP) operations.
 * This service provides methods for generating secret keys, creating QR code URLs,
 * and validating TOTP codes for user authentication.
 */
public interface TotpService {

  /**
   * Generates a new secret key for TOTP authentication.
   *
   * @return a newly generated secret key as a String
   */
  String generateSecretKey();

  /**
   * Generates a QR code URL for the given user and secret key.
   * This URL can be used to display a QR code that users can scan with their TOTP app.
   *
   * @param user the User for whom the QR code is being generated
   * @param secretKey the secret key associated with the user for TOTP authentication
   * @return a String representing the URL of the QR code
   */
  String getQrCodeUrl(User user, String secretKey);

  /**
   * Validates a TOTP code against the provided secret key.
   *
   * @param secretKey the secret key associated with the user for TOTP authentication
   * @param code the TOTP code to be validated
   * @return true if the code is valid for the given secret key, false otherwise
   */
  boolean validateCode(String secretKey, String code);
}
