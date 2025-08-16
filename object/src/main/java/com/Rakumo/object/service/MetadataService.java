package com.Rakumo.object.service;

import com.Rakumo.object.exception.MetadataSyncException;
import com.Rakumo.object.model.LocalObjectReference;

public interface MetadataService {
    void recordMetadata(LocalObjectReference ref) throws MetadataSyncException;
    boolean exists(String bucketName, String objectKey) throws MetadataSyncException;
}