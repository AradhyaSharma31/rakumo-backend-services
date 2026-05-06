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

package com.rakumo.auth.repository;

import com.rakumo.auth.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  /**
   * Finds a user by their email address.
   *
   * @param email the email address to search for.
   * @return an Optional containing the found User, or empty if no user is found.
   */
  Optional<User> findByEmail(String email);

  /**
   * Finds a user by their username.
   *
   * @param username the username to search for.
   * @return an Optional containing the found User, or empty if no user is found.
   */
  Optional<User> findByUsername(String username);

  /**
   * Checks if a user exists with the given email address.
   *
   * @param email the email address to check for existence.
   * @return true if a user exists with the given email, false otherwise.
   */
  Boolean existsByEmail(String email);

  /**
   * Checks if a user exists with the given username.
   *
   * @param username the username to check for existence.
   * @return true if a user exists with the given username, false otherwise.
   */
  Boolean existsByUsername(String username);

  /**
   * Finds a user by their email address and fetches their roles.
   *
   * @param email the email address to search for.
   * @return an Optional containing the found User with roles, or empty if no user is found.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
  Optional<User> findByEmailWithRoles(@Param("email") String email);

  /**
   * Finds a user by their username and fetches their roles.
   *
   * @param id the id of the user to search for.
   * @return an Optional containing the found User with roles, or empty if no user is found.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
  Optional<User> findByIdWithRoles(@Param("id") UUID id);
}
