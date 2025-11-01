package com.Rakumo.auth.service.impl;

import com.Rakumo.auth.entity.RefreshToken;
import com.Rakumo.auth.entity.User;
import com.Rakumo.auth.exception.TokenRefreshException;
import com.Rakumo.auth.repository.RefreshTokenRepository;
import com.Rakumo.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenDuration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.info("Creating refresh token for user: {}", user.getEmail());

        // Delete any existing refresh token for this user
        deleteByUser(user);

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDuration));
        refreshToken.setToken(UUID.randomUUID().toString()); // Using UUID as token

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created with ID: {}", savedToken.getId());

        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        log.debug("Looking up refresh token");
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        log.debug("Verifying refresh token expiration for user: {}", token.getUser().getEmail());

        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            // Token expired, delete it
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expired and deleted for user: {}", token.getUser().getEmail());
            throw TokenRefreshException.expired();
        }

        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        log.debug("Deleting refresh tokens for user: {}", user.getEmail());
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        int deletedCount = refreshTokenRepository.deleteAllExpiredSince(Instant.now());
        log.info("Deleted {} expired refresh tokens", deletedCount);
    }

    // Additional utility methods
    @Transactional(readOnly = true)
    public boolean hasValidRefreshToken(User user) {
        return refreshTokenRepository.findByUser(user)
                .map(token -> !token.isExpired())
                .orElse(false);
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
        log.debug("Refresh token revoked");
    }
}