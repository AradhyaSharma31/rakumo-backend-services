package com.Rakumo.auth.service;

import com.Rakumo.auth.entity.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    String getUserIdFromToken(String token);
    boolean isTokenExpired(String token);
}