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
 * Exception thrown when attempting to create a user that already exists.
 */
public class UserAlreadyExistsException extends AuthException {

  @Serial
  private static final long serialVersionUID = 1729789858816795671L;

  /**
   * Constructs a new UserAlreadyExistsException with the specified detail message.
   *
   * @param message the detail message
   */
  public UserAlreadyExistsException(String message) {
    super(message, "USER_ALREADY_EXISTS");
  }

  /**
   * Constructs a new UserAlreadyExistsException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public UserAlreadyExistsException(String message, Throwable cause) {
    super(message, "USER_ALREADY_EXISTS", cause);
  }

  /**
   * Factory method to create a UserAlreadyExistsException with a message indicating the email that already exists.
   *
   * @param email the email that already exists
   * @return a new UserAlreadyExistsException with the appropriate message
   */
  public static UserAlreadyExistsException withEmail(String email) {
    return new UserAlreadyExistsException("User with email '" + email + "' already exists");
  }

  /**
   * Factory method to create a UserAlreadyExistsException with a message indicating the username that already exists.
   *
   * @param username the username that already exists
   * @return a new UserAlreadyExistsException with the appropriate message
   */
  public static UserAlreadyExistsException withUsername(String username) {
    return new UserAlreadyExistsException("User with username '" + username + "' already exists");
  }
}
