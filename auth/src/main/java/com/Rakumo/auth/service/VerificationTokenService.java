package com.Rakumo.auth.service;

import com.Rakumo.auth.entity.User;
import com.Rakumo.auth.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenService {
    VerificationToken createVerificationToken(User user, String secretKey);
    Optional<VerificationToken> findByToken(String token);
    VerificationToken verifyToken(String token);
    void deleteByUser(User user);
    void deleteExpiredTokens();
    Optional<String> getSecretKeyByUser(User user);

    Optional<VerificationToken> findByUser(User user);
}