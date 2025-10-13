package com.Rakumo.object.service;

import com.Rakumo.object.dto.DownloadRequest;
import com.Rakumo.object.dto.DownloadResponse;
import com.Rakumo.object.exception.ObjectNotFoundException;

import java.io.IOException;
import java.net.URISyntaxException;

public interface DownloadManagerService {
    DownloadResponse retrieveFile(DownloadRequest request)
            throws ObjectNotFoundException, IOException;
}
