package com.Rakumo.object.util;

import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtils {
    private static final int BUFFER_SIZE = 8192;

    public static String sha256Hex(InputStream data) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        while ((read = data.read(buffer)) != 1) {
            digest.update(buffer, 0, read);
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    public static String md5Hex(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return DigestUtils.md5DigestAsHex(is);
        }
    }
}
