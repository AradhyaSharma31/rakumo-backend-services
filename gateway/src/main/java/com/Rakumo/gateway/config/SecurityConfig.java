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

package com.rakumo.gateway.config;

import com.rakumo.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the Gateway service.
 * This class sets up JWT authentication, CORS, and defines access rules for various endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable method-level security
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CorsConfigurationSource corsConfigurationSource;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
                // Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)

                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Session management - stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Security headers
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                        )
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin
                        )
                )

                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                          response.setStatus(401);
                          response.setContentType("application/json");
                          response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                          response.setStatus(403);
                          response.setContentType("application/json");
                          response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
                        })
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/api/auth/refresh-token",
                                "/api/auth/check-token",
                                "/api/objects/download-stream/**",

                                "/api/objects/presigned/generate",           // Generate pre-signed URLs
                                "/api/objects/presigned/validate",           // Validate pre-signed URLs
                                "/api/objects/presigned/download/**",        // Download via pre-signed URLs
                                "/api/objects/presigned/upload/**",          // Upload via pre-signed URLs
                                "/api/objects/presigned/delete/**",          // Delete via pre-signed URLs
                                "/api/objects/presigned/redirect/download/**",
                                "/api/objects/presigned/redirect/upload/**",
                                "/api/objects/presigned/redirect/delete/**",

                                "/error"
                        ).permitAll()

                        // Role-based endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")

                        // Service-specific endpoints
                        .requestMatchers("/api/objects/upload").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/objects/download/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/objects/delete/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/objects/list/**").hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/api/metadata/**").hasAnyRole("USER", "ADMIN")

                        // Auth endpoints that require authentication
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/validate-token").authenticated()
                        .requestMatchers("/api/auth/user-from-token").authenticated()
                        .requestMatchers("/api/auth/change-password").authenticated()

                        // Health check (could be public or restricted)
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
