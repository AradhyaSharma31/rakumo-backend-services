package com.Rakumo.object.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathUtils {

    private static final String ILLEGAL_CHARS = "[^a-zA-Z0-9-_]";

    // Regular files
    public static Path resolveRegularFilePath(String userId, String bucketId, String objectKey, String fileHash) {
        String dir1 = fileHash.substring(0, 2);
        String dir2 = fileHash.substring(2, 4);

        // Extract filename from objectKey
        String fileName = Paths.get(objectKey).getFileName().toString();

        return Paths.get("Object-Storage")
                .resolve(userId)
                .resolve("Regular-Files")
                .resolve(bucketId)
                .resolve(dir1)
                .resolve(dir2)
                .resolve(fileName);
    }


    // Multipart temp files
    public static Path resolveMultipartPath(String userId, String uploadId) {
        return Paths.get("Object-Storage")
                .resolve(userId)
                .resolve("Multipart-Temp")
                .resolve(uploadId);
    }

    public static Path resolveChunkPath(String userId, String uploadId, int chunkIndex) {
        return resolveMultipartPath(userId, uploadId)
                .resolve(String.format("%05d.chunk", chunkIndex));
    }

    public static String sanitize(String input) {
        return input.replaceAll(ILLEGAL_CHARS, "_");
    }

    public static void ensureDirectoryExists(Path path) throws IOException {
        if (!Files.exists(path))
            Files.createDirectories(path);
    }

}
