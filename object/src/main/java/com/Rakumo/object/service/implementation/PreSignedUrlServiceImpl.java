package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.PreSignedUrlRequest;
import com.Rakumo.object.dto.PreSignedUrlResponse;
import com.Rakumo.object.service.PreSignedUrlService;
import com.Rakumo.object.util.ChecksumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreSignedUrlServiceImpl implements PreSignedUrlService {

    private final ChecksumUtils checksumUtils;

    @Value("${app.pre-signed-url.base-url}")
    private String baseUrl;

    @Value("${app.pre-signed-url.secret-key}")
    private String secretKey;

    private final Map<String, PreSignedUrlRequest> urlCache = new ConcurrentHashMap<>();

    @Override
    public PreSignedUrlResponse generatePreSignedUrl(PreSignedUrlRequest request) {
        try {
            // Set default expiration to 1 hour if not provided
            Duration expiration = request.getExpiration() != null ?
                    request.getExpiration() : Duration.ofHours(1);

            Instant expiresAt = Instant.now().plus(expiration);

            // Generate unique token
            String token = generateToken(request, expiresAt);

            // Build the pre-signed URL
            String preSignedUrl = buildPreSignedUrl(request, token, expiresAt);

            // Cache the request for validation
            urlCache.put(token, request);

            log.info("Generated pre-signed URL for {}/{} operation: {}",
                    request.getBucketName(), request.getObjectKey(), request.getOperation());

            return PreSignedUrlResponse.builder()
                    .preSignedUrl(preSignedUrl)
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .versionId(request.getVersionId())
                    .operation(request.getOperation())
                    .expiration(expiresAt)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for {}/{}",
                    request.getBucketName(), request.getObjectKey(), e);
            throw new RuntimeException("Failed to generate pre-signed URL", e);
        }
    }

    @Override
    public boolean validatePreSignedUrl(String url, String bucketName, String objectKey) {
        try {
            String query = extractQueryFromUrl(url);

            if (query == null || !query.contains("token=")) {
                return false;
            }

            String token = extractTokenFromQuery(query);

            if (token == null) {
                return false;
            }

            if (!urlCache.containsKey(token)) {
                return false;
            }

            PreSignedUrlRequest cachedRequest = urlCache.get(token);

            // Validate bucket and object key match
            if (!cachedRequest.getBucketName().equals(bucketName)) {
                return false;
            }

            if (!cachedRequest.getObjectKey().equals(objectKey)) {
                return false;
            }

            // Validate expiration
            Instant expiresAt = extractExpirationFromToken(token);

            if (expiresAt.isBefore(Instant.now())) {
                urlCache.remove(token);
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to extract query from URL
    private String extractQueryFromUrl(String url) {
        int queryIndex = url.indexOf('?');
        if (queryIndex == -1) {
            return null;
        }
        return url.substring(queryIndex + 1);
    }

    private String generateToken(PreSignedUrlRequest request, Instant expiresAt) {
        String data = String.format("%s:%s:%s:%s:%d",
                request.getBucketName(),
                request.getObjectKey(),
                request.getOperation().name(),
                request.getVersionId() != null ? request.getVersionId() : "",
                expiresAt.getEpochSecond());

        log.info("🔐 GENERATING TOKEN:");
        log.info("🔐 Data to sign: {}", data);

        String signature = checksumUtils.hmacSha256(data, secretKey);
        String tokenData = String.format("%s:%d", data, expiresAt.getEpochSecond());

        String token = Base64.getUrlEncoder().encodeToString(tokenData.getBytes()) + "." + signature;
        log.info("🔐 Generated token: {}", token);

        return token;
    }

    private String buildPreSignedUrl(PreSignedUrlRequest request, String token, Instant expiresAt) {
        String endpoint = getEndpointForOperation(request.getOperation());

        return String.format("%s%s/%s/%s?token=%s&expires=%d",
                baseUrl,
                endpoint,
                request.getBucketName(),
                encodeObjectKey(request.getObjectKey()),
                token,
                expiresAt.getEpochSecond());
    }

    private String getEndpointForOperation(PreSignedUrlRequest.PreSignedUrlOperation operation) {
        switch (operation) {
            case DOWNLOAD:
                return "/api/objects/presigned/redirect/download";
            case UPLOAD:
                return "/api/objects/presigned/redirect/upload";
            case DELETE:
                return "/api/objects/presigned/redirect/delete";
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }

    private String encodeObjectKey(String objectKey) {
        return java.net.URLEncoder.encode(objectKey, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    private Instant extractExpirationFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return Instant.MIN;

            String decoded = new String(Base64.getUrlDecoder().decode(parts[0]));
            String[] dataParts = decoded.split(":");
            if (dataParts.length >= 5) {
                long epochSecond = Long.parseLong(dataParts[4]);
                return Instant.ofEpochSecond(epochSecond);
            }
        } catch (Exception e) {
            log.error("Failed to extract expiration from token", e);
        }
        return Instant.MIN;
    }

    // Clean up expired tokens periodically
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        urlCache.entrySet().removeIf(entry -> {
            Instant expiresAt = extractExpirationFromToken(entry.getKey());
            return expiresAt.isBefore(now);
        });
        log.debug("Cleaned up expired pre-signed URL tokens");
    }
}