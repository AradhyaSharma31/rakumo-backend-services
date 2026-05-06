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

import com.rakumo.auth.dto.reponse.UserProfileResponse;
import com.rakumo.auth.dto.request.RegisterRequest;
import com.rakumo.auth.entity.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing user-related operations.
 */
public interface UserService {

  /**
   * Creates a new user based on the provided registration request.
   *
   * @param registerRequest the registration request containing user details
   * @return the created User entity
   */
  User createUser(RegisterRequest registerRequest);

  /**
   * Finds a user by their email address.
   *
   * @param email the email address of the user to find
   * @return an Optional containing the found User, or empty if no user is found
   */
  Optional<User> findByEmail(String email);

  /**
   * Finds a user by their username.
   *
   * @param username the username of the user to find
   * @return an Optional containing the found User, or empty if no user is found
   */
  Optional<User> findById(UUID userId);

  /**
   * Retrieves the profile information of a user by their unique identifier.
   *
   * @param userId the unique identifier of the user
   * @return a UserProfileResponse containing the user's profile information
   */
  UserProfileResponse getUserProfile(UUID userId);

  /**
   * Enables a user account by their unique identifier.
   *
   * @param userId the unique identifier of the user to enable
   */
  void enableUser(UUID userId);

  /**
   * Checks if a user exists with the given email address.
   *
   * @param email the email address to check for existence
   * @return true if a user exists with the given email, false otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Checks if a user exists with the given username.
   *
   * @param username the username to check for existence
   * @return true if a user exists with the given username, false otherwise
   */
  boolean existsByUsername(String username);

  /**
   * Deletes a user account by their unique identifier.
   *
   * @param id the unique identifier of the user to delete
   */
  void deleteUser(UUID id);
}
