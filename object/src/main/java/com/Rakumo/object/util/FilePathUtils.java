package com.Rakumo.object.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilePathUtils {

    private static final String ILLEGAL_CHARS = "[^a-zA-Z0-9-_]";

    public static Path resolvePath(Path baseDir, String bucket, String objectKey, String versionId) {
        String sanitizeBucket = sanitize(bucket);
        String sanitizedKey = sanitize(bucket);
        return baseDir.resolve(sanitizeBucket)
                .resolve(sanitizedKey)
                .resolve(versionId)
                .normalize();
    }

    public static String sanitize(String input) {
        return input.replaceAll(ILLEGAL_CHARS, "_");
    }

    public static void ensureDirectoryExists(Path path) throws IOException {
        if (!Files.exists(path))
            Files.createDirectories(path);
    }

}
