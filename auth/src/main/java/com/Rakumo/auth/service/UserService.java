package com.Rakumo.auth.service;

import com.Rakumo.auth.dto.reponse.UserProfileResponse;
import com.Rakumo.auth.dto.request.RegisterRequest;
import com.Rakumo.auth.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest registerRequest);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID userId);
    UserProfileResponse getUserProfile(UUID userId);
    void enableUser(UUID userId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void deleteUser(UUID id);
}