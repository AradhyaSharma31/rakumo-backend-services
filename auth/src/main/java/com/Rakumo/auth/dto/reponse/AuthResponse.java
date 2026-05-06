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

package com.rakumo.auth.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for authentication operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
  private boolean success;
  private String message;
  private Object data;

  private AuthResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  /**
   * Static factory method to create a successful AuthResponse with a message.
   *
   * @param message the success message to be included in the response
   * @return an AuthResponse object indicating success with the provided message
   */
  public static AuthResponse success(String message) {
    return new AuthResponse(true, message);
  }

  /**
   * Static factory method to create a successful AuthResponse with a message and data.
   *
   * @param message the success message to be included in the response
   * @param data the additional data to be included in the response
   * @return an AuthResponse object indicating success with the provided message and data
   */
  public static AuthResponse success(String message, Object data) {
    return new AuthResponse(true, message, data);
  }

  /**
   * Static factory method to create an error AuthResponse with a message.
   *
   * @param message the error message to be included in the response
   * @return an AuthResponse object indicating failure with the provided message
   */
  public static AuthResponse error(String message) {
    return new AuthResponse(false, message);
  }
}
