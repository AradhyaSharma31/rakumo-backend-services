package com.Rakumo.gateway.controller;

import com.Rakumo.gateway.grpc.*;
import com.Rakumo.gateway.service.GrpcAuthClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthGatewayController {

    private final GrpcAuthClientService authClientService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authClientService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        JwtResponse response = authClientService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        AuthResponse response = authClientService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        JwtResponse response = authClientService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody LogoutRequest request) {
        MessageResponse response = authClientService.logout(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody ValidateTokenRequest request) {
        TokenValidationResponse response = authClientService.validateToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user-from-token")
    public ResponseEntity<UserResponse> getUserFromToken(@RequestBody ValidateTokenRequest request) {
        UserResponse response = authClientService.getUserFromToken(request);
        return ResponseEntity.ok(response);
    }

    // Convenience endpoint for frontend token validation
    @GetMapping("/check-token")
    public ResponseEntity<Boolean> checkToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }
        String token = authHeader.substring(7);
        boolean isValid = authClientService.isValidToken(token);
        return ResponseEntity.ok(isValid);
    }
}