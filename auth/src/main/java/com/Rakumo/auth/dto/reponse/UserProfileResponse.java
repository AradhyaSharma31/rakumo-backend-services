package com.Rakumo.auth.dto.reponse;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserProfileResponse {
    private String id;
    private String email;
    private String username;
    private Boolean isEnabled;
    private List<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}