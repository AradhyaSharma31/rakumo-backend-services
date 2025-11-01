package com.Rakumo.auth.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private Object data;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static AuthResponse success(String message) {
        return new AuthResponse(true, message);
    }

    public static AuthResponse success(String message, Object data) {
        return new AuthResponse(true, message, data);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }
}
