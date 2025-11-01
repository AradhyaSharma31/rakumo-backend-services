package com.Rakumo.auth.service.impl;

import com.Rakumo.auth.entity.User;
import com.Rakumo.auth.service.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TotpServiceImpl implements TotpService {

    private final GoogleAuthenticator gAuth;
    private final String issuer = "Rakumo";

    public TotpServiceImpl() {
        this.gAuth = new GoogleAuthenticator();
    }

    @Override
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secretKey = key.getKey();
        log.info("Generated new TOTP secret key for user");
        return secretKey;
    }

    @Override
    public String getQrCodeUrl(User user, String secretKey) {
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                issuer,
                user.getEmail(),
                new GoogleAuthenticatorKey.Builder(secretKey).build()
        );
        log.info("Generated QR code URL for user: {}", user.getEmail());
        return qrCodeUrl;
    }

    @Override
    public boolean validateCode(String secretKey, String code) {
        try {
            if (code == null || code.length() != 6) {
                log.warn("Invalid TOTP code format: {}", code);
                return false;
            }

            int verificationCode = Integer.parseInt(code);
            boolean isValid = gAuth.authorize(secretKey, verificationCode);

            if (isValid) {
                log.info("TOTP code validation successful");
            } else {
                log.warn("TOTP code validation failed for code: {}", code);
            }

            return isValid;
        } catch (NumberFormatException e) {
            log.error("Invalid TOTP code format (not numeric): {}", code);
            return false;
        } catch (Exception e) {
            log.error("Error validating TOTP code: {}", e.getMessage());
            return false;
        }
    }
}