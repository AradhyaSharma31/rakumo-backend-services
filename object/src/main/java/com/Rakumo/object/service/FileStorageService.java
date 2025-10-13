package com.Rakumo.object.service;

import com.Rakumo.object.entity.RegularObjectEntity;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {
    RegularObjectEntity storeFile(String ownerId, String bucketName, String objectKey, InputStream inputStream,
                                  String contentType, String expectedChecksum)
            throws IOException, ChecksumMismatchException;

    Resource retrieveFile(String bucketName, String objectKey, String versionId)
            throws ObjectNotFoundException, IOException;

    void deleteFile(String bucketName, String objectKey, String versionId)
            throws ObjectNotFoundException, IOException;
}
