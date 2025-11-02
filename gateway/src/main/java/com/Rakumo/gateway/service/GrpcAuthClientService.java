package com.Rakumo.gateway.service;

import com.Rakumo.gateway.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcAuthClientService {

    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;

    public AuthResponse register(RegisterRequest request) {
        try {
            log.info("Calling Auth service register for: {}", request.getEmail());
            return authStub.register(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC register call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    public JwtResponse login(LoginRequest request) {
        try {
            log.info("Calling Auth service login for: {}", request.getEmail());
            return authStub.login(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC login call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    public AuthResponse verifyEmail(EmailVerificationRequest request) {
        try {
            log.info("Calling Auth service verifyEmail for: {}", request.getEmail());
            return authStub.verifyEmail(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC verifyEmail call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    public JwtResponse refreshToken(RefreshTokenRequest request) {
        try {
            log.info("Calling Auth service refreshToken");
            return authStub.refreshToken(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC refreshToken call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    public MessageResponse logout(LogoutRequest request) {
        try {
            log.info("Calling Auth service logout");
            return authStub.logout(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC logout call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    public TokenValidationResponse validateToken(ValidateTokenRequest request) {
        try {
            log.info("Calling Auth service validateToken");
            return authStub.validateToken(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC validateToken call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    public UserResponse getUserFromToken(ValidateTokenRequest request) {
        try {
            log.info("Calling Auth service getUserFromToken");
            return authStub.getUserFromToken(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC getUserFromToken call failed: {}", e.getStatus());
            throw new RuntimeException("Auth service unavailable: " + e.getStatus().getDescription());
        }
    }

    // Utility method for token validation (commonly used)
    public boolean isValidToken(String token) {
        try {
            ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                    .setToken(token)
                    .build();
            TokenValidationResponse response = validateToken(request);
            return response.getValid();
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Utility method to get user info from token
    public UserResponse getUserInfo(String token) {
        ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                .setToken(token)
                .build();
        return getUserFromToken(request);
    }
}