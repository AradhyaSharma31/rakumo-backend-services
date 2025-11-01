package com.Rakumo.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String token;  // This will store the OTP

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate = Instant.now().plus(Duration.ofMinutes(15));

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType = TokenType.EMAIL_VERIFICATION;

    // Helper methods
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }
}