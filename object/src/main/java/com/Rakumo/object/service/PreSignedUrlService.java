package com.Rakumo.object.service;

import com.Rakumo.object.dto.PreSignedUrlRequest;
import com.Rakumo.object.dto.PreSignedUrlResponse;

public interface PreSignedUrlService {
    PreSignedUrlResponse generatePreSignedUrl(PreSignedUrlRequest request);
    boolean validatePreSignedUrl(String url, String bucketName, String objectKey);
}