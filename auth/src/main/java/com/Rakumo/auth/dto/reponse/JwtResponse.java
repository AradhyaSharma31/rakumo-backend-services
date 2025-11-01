package com.Rakumo.auth.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType = "Bearer";
    private String userId;
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, String refreshToken, Long expiresIn,
                       String userId, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }
}