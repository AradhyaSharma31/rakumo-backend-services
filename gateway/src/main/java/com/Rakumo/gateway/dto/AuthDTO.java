package com.Rakumo.gateway.dto;

import java.util.List;

public class AuthDTO {

    // Authentication DTOs
    public record LoginRequestDTO(String email, String password) {}
    public record RegisterRequestDTO(String email, String password, String username) {}
    public record EmailVerificationRequestDTO(String email, String otp) {}
    public record RefreshTokenRequestDTO(String refreshToken) {}
    public record LogoutRequestDTO(String refreshToken) {}
    public record ValidateTokenRequestDTO(String token) {}

    public record AuthDataDTO(String qrCodeUrl, String secretKey, String userId, String email) {}
    public record AuthResponseDTO(Boolean success, String message, AuthDataDTO data) {}

    public record JwtResponseDTO(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String tokenType,
            String userId,
            String email,
            List<String> roles
    ) {}

    public record TokenValidationResponseDTO(
            Boolean valid,
            String userId,
            String email,
            List<String> roles,
            String errorMessage
    ) {}

    public record UserResponseDTO(
            String id,
            String email,
            String username,
            Boolean isEnabled,
            List<String> roles,
            String createdAt,
            String updatedAt
    ) {}

    public record MessageResponseDTO(Boolean success, String message) {}

}
