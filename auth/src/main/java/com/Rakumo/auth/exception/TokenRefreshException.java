package com.Rakumo.auth.exception;

public class TokenRefreshException extends AuthException {

    public TokenRefreshException(String message) {
        super(message, "TOKEN_REFRESH_ERROR");
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, "TOKEN_REFRESH_ERROR", cause);
    }

    // Convenience constructors
    public static TokenRefreshException expired() {
        return new TokenRefreshException("Refresh token has expired");
    }

    public static TokenRefreshException notFound() {
        return new TokenRefreshException("Refresh token not found");
    }

    public static TokenRefreshException invalid() {
        return new TokenRefreshException("Refresh token is invalid");
    }
}