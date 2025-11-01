package com.Rakumo.auth.service;

import com.Rakumo.auth.entity.RefreshToken;
import com.Rakumo.auth.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUser(User user);
    void deleteExpiredTokens();
}