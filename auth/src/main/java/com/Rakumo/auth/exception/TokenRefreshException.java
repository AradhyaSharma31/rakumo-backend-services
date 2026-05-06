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

package com.rakumo.auth.exception;

import java.io.Serial;

/**
 * Exception thrown when there is an error during the token refresh process.
 */
public class TokenRefreshException extends AuthException {

  @Serial
  private static final long serialVersionUID = 7628904955766528273L;

  /**
   * Constructs a new TokenRefreshException with the specified detail message.
   *
   * @param message the detail message
   */
  public TokenRefreshException(String message) {
    super(message, "TOKEN_REFRESH_ERROR");
  }

  /**
   * Constructs a new TokenRefreshException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public TokenRefreshException(String message, Throwable cause) {
    super(message, "TOKEN_REFRESH_ERROR", cause);
  }

  /**
   * Factory method to create a TokenRefreshException indicating that the refresh token has expired.
   *
   * @return a new instance of TokenRefreshException with a message indicating expiration
   */
  public static TokenRefreshException expired() {
    return new TokenRefreshException("Refresh token has expired");
  }

  /**
   * Factory method to create a TokenRefreshException
   *
   * @return a new instance of TokenRefreshException with a message
   */
  public static TokenRefreshException notFound() {
    return new TokenRefreshException("Refresh token not found");
  }

  /**
   * Factory method to create a TokenRefreshException indicating that the refresh token is invalid.
   *
   * @return a new instance of TokenRefreshException with a message indicating invalidity
   */
  public static TokenRefreshException invalid() {
    return new TokenRefreshException("Refresh token is invalid");
  }
}
