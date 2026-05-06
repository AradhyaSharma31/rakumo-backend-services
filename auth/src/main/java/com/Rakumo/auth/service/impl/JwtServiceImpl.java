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

import com.rakumo.auth.entity.User;
import com.rakumo.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JwtServiceImpl is an implementation of the JwtService interface.
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${app.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId().toString());
    claims.put("email", user.getEmail());
    claims.put("roles", user.getRoles());

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getEmail())
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(accessTokenExpiration)))
            .signWith(getSigningKey())
            .compact();
  }

  @Override
  public String generateRefreshToken(User user) {
    return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getId().toString())
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(refreshTokenExpiration)))
            .signWith(getSigningKey())
            .compact();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token);

      return true;
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
      return false;
    } catch (SecurityException e) {
      log.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  @Override
  public String getUsernameFromToken(String token) {
    return getClaimsFromToken(token).getSubject();
  }

  @Override
  public String getUserIdFromToken(String token) {
    return getClaimsFromToken(token).get("userId", String.class);
  }

  @Override
  public boolean isTokenExpired(String token) {
    return getClaimsFromToken(token).getExpiration().before(new Date());
  }

  private Claims getClaimsFromToken(String token) {
    try {
      return Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
    } catch (Exception e) {
      log.error("Error parsing JWT token: {}", e.getMessage());
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }
}
