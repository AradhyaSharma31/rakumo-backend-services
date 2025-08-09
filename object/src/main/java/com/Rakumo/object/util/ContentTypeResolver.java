package com.Rakumo.object.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ContentTypeResolver {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // ======================
        // Images
        // ======================
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("tiff", "image/tiff");
        MIME_TYPES.put("psd", "image/vnd.adobe.photoshop");

        // ======================
        // Documents
        // ======================
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("rtf", "application/rtf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPES.put("odt", "application/vnd.oasis.opendocument.text");
        MIME_TYPES.put("ods", "application/vnd.oasis.opendocument.spreadsheet");

        // ======================
        // Archives
        // ======================
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("rar", "application/x-rar-compressed");
        MIME_TYPES.put("tar", "application/x-tar");
        MIME_TYPES.put("gz", "application/gzip");
        MIME_TYPES.put("7z", "application/x-7z-compressed");

        // ======================
        // Audio/Video
        // ======================
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("ogg", "audio/ogg");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("avi", "video/x-msvideo");
        MIME_TYPES.put("mkv", "video/x-matroska");

        // ======================
        // Code/Data
        // ======================
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("csv", "text/csv");
        MIME_TYPES.put("js", "text/javascript");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("sql", "application/sql");

        // ======================
        // System/Executables
        // ======================
        MIME_TYPES.put("exe", "application/x-msdownload");
        MIME_TYPES.put("dmg", "application/x-apple-diskimage");
        MIME_TYPES.put("apk", "application/vnd.android.package-archive");
        MIME_TYPES.put("deb", "application/x-debian-package");
        MIME_TYPES.put("rpm", "application/x-redhat-package-manager");
    }

    public static String resolve(Path file) throws IOException {
        String detectedType = Files.probeContentType(file);
        if (detectedType != null) {
            return detectedType;
        }

        String fileName = file.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return MIME_TYPES.getOrDefault(extension.toLowerCase(),
                "application/octet-stream");
    }

    public static String resolveFromFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        return MIME_TYPES.getOrDefault(extension.toLowerCase(),
                "application/octet-stream");
    }
}
