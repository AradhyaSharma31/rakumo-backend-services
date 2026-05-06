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
 * Custom exception class for authentication-related errors in the Rakumo application.
 * This exception can be thrown when there are issues with user authentication, such as
 * invalid credentials, expired tokens, or insufficient permissions.
 */
public class AuthException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 6812311704259512482L;

  private final String errorCode;

  /**
   * Constructs a new AuthException with the specified detail message and a default error code.
   *
   * @param message the detail message explaining the reason for the exception
   */
  public AuthException(String message) {
    super(message);
    errorCode = "AUTH_ERROR";
  }

  /**
   * Constructs a new AuthException with the specified detail message and error code.
   *
   * @param message the detail message explaining the reason for the exception
   * @param errorCode a specific error code representing the type of authentication error
   */
  public AuthException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * Constructs a new AuthException with the detail message, cause, and a default error code.
   *
   * @param message the detail message explaining the reason for the exception
   * @param cause the cause of the exception
   */
  public AuthException(String message, Throwable cause) {
    super(message, cause);
    errorCode = "AUTH_ERROR";
  }

  /**
   * Constructs a new AuthException with the specified detail message, error code, and cause.
   *
   * @param message the detail message explaining the reason for the exception
   * @param errorCode a specific error code representing the type of authentication error
   * @param cause the cause of the exception
   */
  public AuthException(String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
