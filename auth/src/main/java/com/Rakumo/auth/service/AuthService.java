package com.Rakumo.auth.service;

import com.Rakumo.auth.dto.reponse.AuthResponse;
import com.Rakumo.auth.dto.reponse.JwtResponse;
import com.Rakumo.auth.dto.request.LoginRequest;
import com.Rakumo.auth.dto.request.RegisterRequest;
import com.Rakumo.auth.dto.request.EmailVerificationRequest;
import com.Rakumo.auth.dto.request.RefreshTokenRequest;
import org.apache.http.auth.InvalidCredentialsException;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    JwtResponse login(LoginRequest loginRequest) throws InvalidCredentialsException;
    AuthResponse verifyEmail(EmailVerificationRequest verificationRequest);
    JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    void logout(String refreshToken);
}