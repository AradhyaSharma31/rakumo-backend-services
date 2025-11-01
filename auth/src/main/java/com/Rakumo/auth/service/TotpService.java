package com.Rakumo.auth.service;

import com.Rakumo.auth.entity.User;

public interface TotpService {
    String generateSecretKey();
    String getQrCodeUrl(User user, String secretKey);
    boolean validateCode(String secretKey, String code);
}