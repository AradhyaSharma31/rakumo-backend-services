package com.Rakumo.gateway.controller;

import com.Rakumo.auth.grpc.*;
import com.Rakumo.gateway.dto.AuthDTO.*;
import com.Rakumo.gateway.mapper.GrpcMapper;
import com.Rakumo.gateway.service.GrpcAuthClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthGatewayController {

    private final GrpcAuthClientService authClientService;
    private final GrpcMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO requestDTO) {
        RegisterRequest grpcRequest = mapper.toGrpcRegister(requestDTO);
        AuthResponse grpcResponse = authClientService.register(grpcRequest);
        AuthResponseDTO responseDTO = mapper.toDtoAuth(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@RequestBody LoginRequestDTO requestDTO) {
        LoginRequest grpcRequest = mapper.toGrpcLogin(requestDTO);
        JwtResponse grpcResponse = authClientService.login(grpcRequest);
        JwtResponseDTO responseDTO = mapper.toDtoJwt(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponseDTO> verifyEmail(@RequestBody EmailVerificationRequestDTO requestDTO) {
        EmailVerificationRequest grpcRequest = mapper.toGrpcEmailVerify(requestDTO);
        AuthResponse grpcResponse = authClientService.verifyEmail(grpcRequest);
        AuthResponseDTO responseDTO = mapper.toDtoAuth(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO requestDTO) {
        RefreshTokenRequest grpcRequest = mapper.toGrpcRefreshToken(requestDTO);
        JwtResponse grpcResponse = authClientService.refreshToken(grpcRequest);
        JwtResponseDTO responseDTO = mapper.toDtoJwt(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(@RequestBody LogoutRequestDTO requestDTO) {
        LogoutRequest grpcRequest = mapper.toGrpcLogout(requestDTO);
        MessageResponse grpcResponse = authClientService.logout(grpcRequest);
        MessageResponseDTO responseDTO = mapper.toDtoMessage(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponseDTO> validateToken(@RequestBody ValidateTokenRequestDTO requestDTO) {
        ValidateTokenRequest grpcRequest = mapper.toGrpcValidateToken(requestDTO);
        TokenValidationResponse grpcResponse = authClientService.validateToken(grpcRequest);
        TokenValidationResponseDTO responseDTO = mapper.toDtoTokenValidation(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/user-from-token")
    public ResponseEntity<UserResponseDTO> getUserFromToken(@RequestBody ValidateTokenRequestDTO requestDTO) {
        ValidateTokenRequest grpcRequest = mapper.toGrpcValidateToken(requestDTO);
        UserResponse grpcResponse = authClientService.getUserFromToken(grpcRequest);
        UserResponseDTO responseDTO = mapper.toDtoUser(grpcResponse);
        return ResponseEntity.ok(responseDTO);
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