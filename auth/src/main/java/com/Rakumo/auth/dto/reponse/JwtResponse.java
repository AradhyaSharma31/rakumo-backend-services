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

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for JWT authentication
 * containing access token, refresh token, expiration time, user details, and roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
  private String accessToken;
  private String refreshToken;
  private Long expiresIn;
  private String tokenType = "Bearer";
  private String userId;
  private String email;
  private List<String> roles;

  /**
   * Constructor for creating a JWT response with all necessary fields.
   *
   * @param accessToken  the JWT access token
   * @param refreshToken the JWT refresh token
   * @param expiresIn    the expiration time of the access token in seconds
   * @param userId       the unique identifier of the authenticated user
   * @param email        the email address of the authenticated user
   * @param roles        the list of roles assigned to the authenticated user
   */
  public JwtResponse(String accessToken, String refreshToken, Long expiresIn,
                     String userId, String email, List<String> roles) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresIn = expiresIn;
    this.userId = userId;
    this.email = email;

    if (roles != null) {
      this.roles = new ArrayList<>(roles);
    } else {
      this.roles = new ArrayList<>();
    }
  }
}
