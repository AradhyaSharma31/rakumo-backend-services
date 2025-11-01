package com.Rakumo.auth.service.impl;

import com.Rakumo.auth.dto.reponse.AuthResponse;
import com.Rakumo.auth.dto.reponse.JwtResponse;
import com.Rakumo.auth.dto.request.*;
import com.Rakumo.auth.entity.User;
import com.Rakumo.auth.entity.VerificationToken;
import com.Rakumo.auth.exception.*;
import com.Rakumo.auth.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        verificationTokenService.deleteExpiredTokens();

        // Also check for existing expired user with same email
        Optional<User> existingUser = userService.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getIsEnabled()) {
            // Check if this user has an expired verification token
            Optional<VerificationToken> existingToken =
                    verificationTokenService.findByUser(existingUser.get());
            if (existingToken.isPresent() && existingToken.get().isExpired()) {
                // Delete the expired registration
                userService.deleteUser(existingUser.get().getId());
                log.info("Cleaned up expired registration for: {}", registerRequest.getEmail());
            }
        }

        log.info("Starting registration for email: {}", registerRequest.getEmail());

        // Validate input
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            throw new AuthException("Email is required");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new AuthException("Password is required");
        }

        // Check if user already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw UserAlreadyExistsException.withEmail(registerRequest.getEmail());
        }

        if (registerRequest.getUsername() != null &&
                !registerRequest.getUsername().trim().isEmpty() &&
                userService.existsByUsername(registerRequest.getUsername())) {
            throw UserAlreadyExistsException.withUsername(registerRequest.getUsername());
        }

        // ✅ Create user (DISABLED - cannot login yet)
        User user = userService.createUser(registerRequest);
        log.info("User created but disabled: {}", user.getEmail());

        // Generate TOTP secret and QR code
        String secretKey = totpService.generateSecretKey();
        String qrCodeUrl = totpService.getQrCodeUrl(user, secretKey);

        // Store TOTP secret for verification
        verificationTokenService.createVerificationToken(user, secretKey);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("qrCodeUrl", qrCodeUrl);
        responseData.put("secretKey", secretKey); // For manual entry if needed
        responseData.put("userId", user.getId().toString());
        responseData.put("email", user.getEmail());

        log.info("TOTP setup required for user: {}", user.getEmail());
        return AuthResponse.success(
                "Please scan the QR code with Google Authenticator and enter the code to complete registration.",
                responseData
        );
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(EmailVerificationRequest verificationRequest) {
        log.info("TOTP verification attempt for user: {}", verificationRequest.getEmail());

        // Validate input
        if (verificationRequest.getEmail() == null || verificationRequest.getEmail().trim().isEmpty()) {
            throw new AuthException("Email is required");
        }
        if (verificationRequest.getOtp() == null || verificationRequest.getOtp().trim().isEmpty()) {
            throw new AuthException("TOTP code is required");
        }

        // Find user
        User user = userService.findByEmail(verificationRequest.getEmail())
                .orElseThrow(() -> new AuthException("User not found"));

        // Check if user is already enabled (prevent re-verification)
        if (user.getIsEnabled()) {
            return AuthResponse.success("Account is already verified.");
        }

        // Get stored TOTP secret
        String secretKey = verificationTokenService.getSecretKeyByUser(user)
                .orElseThrow(() -> new AuthException("No TOTP setup found for user. Please register again."));

        // Validate TOTP code
        if (!totpService.validateCode(secretKey, verificationRequest.getOtp())) {
            throw new AuthException("Invalid TOTP code. Please try again.");
        }

        // ENABLE USER ACCOUNT (this is when registration completes)
        userService.enableUser(user.getId());

        // Clean up verification token
        verificationTokenService.deleteByUser(user);

        log.info("TOTP verification successful! User fully registered: {}", user.getEmail());
        return AuthResponse.success("Registration completed successfully! You can now login.");
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // Validate input
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new AuthException("Email is required");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new AuthException("Password is required");
        }

        // Find user
        User user = userService.findByEmail(loginRequest.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // Check if user is enabled (TOTP verified) - CRITICAL!
        if (!user.getIsEnabled()) {
            throw new AuthException("Account not verified. Please complete TOTP setup first.");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token
        refreshTokenService.createRefreshToken(user);

        log.info("Login successful for user: {}", user.getEmail());
        return new JwtResponse(
                accessToken,
                refreshToken,
                900L,
                user.getId().toString(),
                user.getEmail(),
                user.getRoles()
        );
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("Refresh token request");

        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        // Validate input
        if (requestRefreshToken == null || requestRefreshToken.trim().isEmpty()) {
            throw new AuthException("Refresh token is required");
        }

        // Validate refresh token structure
        if (!jwtService.validateToken(requestRefreshToken)) {
            throw TokenRefreshException.invalid();
        }

        // Find stored refresh token
        var refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(TokenRefreshException::notFound);

        // Check expiration
        refreshTokenService.verifyExpiration(refreshToken);

        // Get user and generate new tokens
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Update refresh token (creates new, deletes old)
        refreshTokenService.createRefreshToken(user);

        log.info("Token refresh successful for user: {}", user.getEmail());
        return new JwtResponse(
                newAccessToken,
                newRefreshToken,
                900L, // 15 minutes in seconds
                user.getId().toString(),
                user.getEmail(),
                user.getRoles()
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout request");

        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(token -> {
                        refreshTokenService.deleteByUser(token.getUser());
                        log.info("Refresh token revoked for user: {}", token.getUser().getEmail());
                    });
        }
    }

    // Additional utility method for token validation
    @Transactional(readOnly = true)
    public boolean validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }
        return jwtService.validateToken(accessToken);
    }

    // Additional utility method to get user from token
    @Transactional(readOnly = true)
    public User getUserFromToken(String accessToken) {
        if (!validateAccessToken(accessToken)) {
            throw new AuthException("Invalid access token");
        }

        String userId = jwtService.getUserIdFromToken(accessToken);
        return userService.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new AuthException("User not found"));
    }
}