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

package com.rakumo.gateway.filter;

import com.rakumo.gateway.service.GrpcAuthClientService;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that intercepts incoming HTTP requests to validate JWT tokens.
 * It extracts the token from the Authorization header, validates it using the Auth service,
 * and sets the authentication in the security context if valid.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final GrpcAuthClientService authClientService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        boolean isValid = authClientService.isValidToken(token);

        if (isValid) {
          var userResponse = authClientService.getUserInfo(token);

          if (!userResponse.getIsEnabled()) {
            log.warn("Authentication attempted with disabled account: {}", userResponse.getEmail());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Account disabled");
            return;
          }

          List<SimpleGrantedAuthority> authorities = userResponse.getRolesList().stream()
                  .map(role -> {
                    if (role.startsWith("ROLE_")) {
                      return role;
                    }
                    return "ROLE_" + role;
                  })
                  .map(SimpleGrantedAuthority::new)
                  .collect(Collectors.toList());

          UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                          userResponse.getEmail(),
                          null,
                          authorities
                  );

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.debug("Authenticated user: {} with roles: {}", userResponse.getEmail(), authorities);
        } else {
          log.warn("Invalid JWT token received");
          SecurityContextHolder.clearContext();
        }
      } catch (StatusRuntimeException e) {
        log.error("Auth service unavailable during token validation: {}", e.getStatus());
        SecurityContextHolder.clearContext();
        sendErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Authentication service unavailable");
        return;
      } catch (Exception e) {
        log.error("JWT validation failed: {}", e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  private static void sendErrorResponse(HttpServletResponse response, int status, String message)
          throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    if (status == 403) {
      response.getWriter().write(String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
              "Forbidden", message));
    } else {
      response.getWriter().write(String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
              "Service Unavailable", message));
    }
  }
}
