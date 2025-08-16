package com.Rakumo.object.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.CRC32;

public final class ChecksumUtils {
    private static final int STREAM_BUFFER_SIZE = 8192; // 8KB buffer
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    // Prevent instantiation
    private ChecksumUtils() {}

    public static String sha256(Path file) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            return sha256(is);
        }
    }

    public static String sha256(InputStream data) throws IOException {
        return hashStream(data, "SHA-256");
    }

    public static String md5(Path file) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not supported", e);
        }
    }

    public static boolean verify(Path file, String expectedChecksum) throws IOException {
        if (expectedChecksum == null) return true;
        String actual = sha256(file);
        return actual.equals(expectedChecksum);
    }

    private static String hashStream(InputStream data, String algorithm)
            throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = data.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return HEX_FORMAT.formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " not supported", e);
        }
    }

    public static long crc32(Path file) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            return crc32(is);
        }
    }

    public static long crc32(InputStream data) throws IOException {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[STREAM_BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = data.read(buffer)) != -1) {
            crc.update(buffer, 0, bytesRead);
        }

        return crc.getValue();
    }
}