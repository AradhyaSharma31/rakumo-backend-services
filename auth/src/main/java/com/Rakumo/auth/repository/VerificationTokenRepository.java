package com.Rakumo.auth.repository;

import com.Rakumo.auth.entity.User;
import com.Rakumo.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < :now")
    int deleteAllExpiredSince(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user AND vt.tokenType = :tokenType")
    void deleteByUserAndTokenType(@Param("user") User user, @Param("tokenType") VerificationToken.TokenType tokenType);
}