package com.Rakumo.auth.exception;

public class UserAlreadyExistsException extends AuthException {

    public UserAlreadyExistsException(String message) {
        super(message, "USER_ALREADY_EXISTS");
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, "USER_ALREADY_EXISTS", cause);
    }

    // Convenience constructors
    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException("User with email '" + email + "' already exists");
    }

    public static UserAlreadyExistsException withUsername(String username) {
        return new UserAlreadyExistsException("User with username '" + username + "' already exists");
    }
}