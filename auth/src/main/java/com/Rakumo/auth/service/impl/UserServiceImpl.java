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

import com.rakumo.auth.dto.reponse.UserProfileResponse;
import com.rakumo.auth.dto.request.RegisterRequest;
import com.rakumo.auth.entity.User;
import com.rakumo.auth.exception.UserAlreadyExistsException;
import com.rakumo.auth.repository.UserRepository;
import com.rakumo.auth.service.UserService;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService for managing user accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public User createUser(RegisterRequest registerRequest) {
    log.info("Creating new user with email: {}", registerRequest.getEmail());

    // Double-check existence
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw UserAlreadyExistsException.withEmail(registerRequest.getEmail());
    }

    if (registerRequest.getUsername() != null
            && userRepository.existsByUsername(registerRequest.getUsername())) {
      throw UserAlreadyExistsException.withUsername(registerRequest.getUsername());
    }

    User user = new User();
    user.setEmail(registerRequest.getEmail());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    user.setUsername(registerRequest.getUsername());
    user.setIsEnabled(false); // Disabled until TOTP verification
    user.setRoles(Collections.singletonList("ROLE_USER"));

    User savedUser = userRepository.save(user);
    log.info("User created with ID: {} (disabled until TOTP verification)", savedUser.getId());

    return savedUser;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    log.debug("Finding user by email: {}", email);
    return userRepository.findByEmailWithRoles(email);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(UUID userId) {
    log.debug("Finding user by ID: {}", userId);
    return userRepository.findByIdWithRoles(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(UUID userId) {
    log.info("Fetching user profile for ID: {}", userId);

    User user = userRepository.findByIdWithRoles(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    return mapToUserProfileResponse(user);
  }

  @Override
  @Transactional
  public void enableUser(UUID userId) {
    log.info("Enabling user account with ID: {}", userId);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    user.setIsEnabled(true);
    userRepository.save(user);

    log.info("User account enabled for: {}", user.getEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  @Override
  @Transactional
  public void deleteUser(UUID userId) {
    log.info("Deleting user with ID: {}", userId);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    userRepository.delete(user);

    log.info("User deleted successfully: {}", user.getEmail());
  }

  /**
   * Utility method to map User entity to UserProfileResponse DTO.
   */
  private static UserProfileResponse mapToUserProfileResponse(User user) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(user.getId().toString());
    response.setEmail(user.getEmail());
    response.setUsername(user.getUsername());
    response.setIsEnabled(user.getIsEnabled());
    response.setRoles(user.getRoles());
    response.setCreatedAt(user.getCreatedAt());
    response.setUpdatedAt(user.getUpdatedAt());

    return response;
  }

  /**
   * Additional method to check if a user is enabled, which can be useful for
   * authentication and authorization checks.
   */
  @Transactional(readOnly = true)
  private boolean isUserEnabled(UUID userId) {
    return userRepository.findById(userId)
            .map(User::getIsEnabled)
            .orElse(false);
  }

  /**
   * Method to update a user's username, ensuring that the new username is unique.
   */
  @Transactional
  private void updateUsername(UUID userId, String newUsername) {
    log.info("Updating username for user ID: {}", userId);

    if (userRepository.existsByUsername(newUsername)) {
      throw new IllegalArgumentException("Username already exists: " + newUsername);
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setUsername(newUsername);
    userRepository.save(user);

    log.info("Username updated successfully for user: {}", user.getEmail());
  }
}
