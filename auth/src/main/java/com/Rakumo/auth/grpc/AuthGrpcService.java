package com.Rakumo.auth.grpc;

import com.Rakumo.auth.entity.User;
import com.Rakumo.auth.exception.GlobalExceptionHandler;
import com.Rakumo.auth.service.AuthService;
import com.Rakumo.auth.service.JwtService;
import com.Rakumo.auth.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import com.Rakumo.auth.grpc.AuthResponse;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;
    private final GlobalExceptionHandler exceptionHandler;

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            log.info("gRPC Register request for email: {}", request.getEmail());

            com.Rakumo.auth.dto.request.RegisterRequest registerRequest =
                    new com.Rakumo.auth.dto.request.RegisterRequest();
            registerRequest.setEmail(request.getEmail());
            registerRequest.setPassword(request.getPassword());
            registerRequest.setUsername(request.getUsername());

            com.Rakumo.auth.dto.reponse.AuthResponse authResponse = authService.register(registerRequest);

            com.Rakumo.auth.grpc.AuthResponse.Builder responseBuilder =
                    com.Rakumo.auth.grpc.AuthResponse.newBuilder()
                            .setSuccess(authResponse.isSuccess())
                            .setMessage(authResponse.getMessage());

            if (authResponse.getData() != null) {
                var data = authResponse.getData();
                if (data instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) data;

                    com.Rakumo.auth.grpc.AuthData.Builder dataBuilder =
                            com.Rakumo.auth.grpc.AuthData.newBuilder();

                    if (dataMap.get("qrCodeUrl") != null) {
                        dataBuilder.setQrCodeUrl(dataMap.get("qrCodeUrl").toString());
                    }
                    if (dataMap.get("secretKey") != null) {
                        dataBuilder.setSecretKey(dataMap.get("secretKey").toString());
                    }
                    if (dataMap.get("userId") != null) {
                        dataBuilder.setUserId(dataMap.get("userId").toString());
                    }
                    if (dataMap.get("email") != null) {
                        dataBuilder.setEmail(dataMap.get("email").toString());
                    }

                    responseBuilder.setData(dataBuilder.build());
                }
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC register: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<JwtResponse> responseObserver) {
        try {
            log.info("gRPC Login request for email: {}", request.getEmail());

            com.Rakumo.auth.dto.request.LoginRequest loginRequest =
                    new com.Rakumo.auth.dto.request.LoginRequest();
            loginRequest.setEmail(request.getEmail());
            loginRequest.setPassword(request.getPassword());

            com.Rakumo.auth.dto.reponse.JwtResponse jwtResponse = authService.login(loginRequest);

            com.Rakumo.auth.grpc.JwtResponse response =
                    com.Rakumo.auth.grpc.JwtResponse.newBuilder()
                            .setAccessToken(jwtResponse.getAccessToken())
                            .setRefreshToken(jwtResponse.getRefreshToken())
                            .setExpiresIn(jwtResponse.getExpiresIn())
                            .setTokenType(jwtResponse.getTokenType())
                            .setUserId(jwtResponse.getUserId())
                            .setEmail(jwtResponse.getEmail())
                            .addAllRoles(jwtResponse.getRoles())
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC login: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }

    @Override
    public void verifyEmail(EmailVerificationRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            log.info("gRPC VerifyEmail request for email: {}", request.getEmail());

            com.Rakumo.auth.dto.request.EmailVerificationRequest verificationRequest =
                    new com.Rakumo.auth.dto.request.EmailVerificationRequest();
            verificationRequest.setEmail(request.getEmail());
            verificationRequest.setOtp(request.getOtp());

            com.Rakumo.auth.dto.reponse.AuthResponse authResponse = authService.verifyEmail(verificationRequest);

            com.Rakumo.auth.grpc.AuthResponse response =
                    com.Rakumo.auth.grpc.AuthResponse.newBuilder()
                            .setSuccess(authResponse.isSuccess())
                            .setMessage(authResponse.getMessage())
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC verifyEmail: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<JwtResponse> responseObserver) {
        try {
            log.info("gRPC RefreshToken request");

            com.Rakumo.auth.dto.request.RefreshTokenRequest refreshTokenRequest =
                    new com.Rakumo.auth.dto.request.RefreshTokenRequest();
            refreshTokenRequest.setRefreshToken(request.getRefreshToken());

            com.Rakumo.auth.dto.reponse.JwtResponse jwtResponse = authService.refreshToken(refreshTokenRequest);

            com.Rakumo.auth.grpc.JwtResponse response =
                    com.Rakumo.auth.grpc.JwtResponse.newBuilder()
                            .setAccessToken(jwtResponse.getAccessToken())
                            .setRefreshToken(jwtResponse.getRefreshToken())
                            .setExpiresIn(jwtResponse.getExpiresIn())
                            .setTokenType(jwtResponse.getTokenType())
                            .setUserId(jwtResponse.getUserId())
                            .setEmail(jwtResponse.getEmail())
                            .addAllRoles(jwtResponse.getRoles())
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC refreshToken: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<MessageResponse> responseObserver) {
        try {
            log.info("gRPC Logout request");

            authService.logout(request.getRefreshToken());

            com.Rakumo.auth.grpc.MessageResponse response =
                    com.Rakumo.auth.grpc.MessageResponse.newBuilder()
                            .setSuccess(true)
                            .setMessage("Logout successful")
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC logout: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<TokenValidationResponse> responseObserver) {
        try {
            log.info("gRPC ValidateToken request");

            String token = request.getToken();
            boolean isValid = jwtService.validateToken(token);

            com.Rakumo.auth.grpc.TokenValidationResponse.Builder responseBuilder =
                    com.Rakumo.auth.grpc.TokenValidationResponse.newBuilder()
                            .setValid(isValid);

            if (isValid) {
                String userId = jwtService.getUserIdFromToken(token);
                String email = jwtService.getUsernameFromToken(token);

                // Get user to fetch roles
                User user = userService.findById(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("User not found"));

                responseBuilder
                        .setUserId(userId)
                        .setEmail(email)
                        .addAllRoles(user.getRoles());
            } else {
                responseBuilder.setErrorMessage("Invalid token");
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC validateToken: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }

    @Override
    public void getUserFromToken(ValidateTokenRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            log.info("gRPC GetUserFromToken request");

            String token = request.getToken();

            if (!jwtService.validateToken(token)) {
                throw new RuntimeException("Invalid token");
            }

            String userId = jwtService.getUserIdFromToken(token);
            User user = userService.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            com.Rakumo.auth.grpc.UserResponse response =
                    com.Rakumo.auth.grpc.UserResponse.newBuilder()
                            .setId(user.getId().toString())
                            .setEmail(user.getEmail())
                            .setUsername(user.getUsername() != null ? user.getUsername() : "")
                            .setIsEnabled(user.getIsEnabled())
                            .addAllRoles(user.getRoles())
                            .setCreatedAt(user.getCreatedAt().toString())
                            .setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "")
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in gRPC getUserFromToken: {}", e.getMessage());
            responseObserver.onError(exceptionHandler.handleException(e));
        }
    }
}