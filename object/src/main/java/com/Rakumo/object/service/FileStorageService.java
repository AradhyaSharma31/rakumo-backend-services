package com.Rakumo.object.service;

import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.IncompleteUploadException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.model.FileChunkInfo;
import com.Rakumo.object.model.LocalObjectReference;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    void storeCompleteFile(LocalObjectReference ref, InputStream data)
            throws IOException, ChecksumMismatchException;

    void storeChunk(FileChunkInfo chunk) throws IOException;

    void assembleChunks(String uploadId, LocalObjectReference finalRef)
            throws IOException, IncompleteUploadException;

    InputStream retrieveFile(LocalObjectReference ref) throws ObjectNotFoundException, IOException;

    void deleteFile(LocalObjectReference ref) throws IOException;
}
