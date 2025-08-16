package com.Rakumo.object.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils() {} // Prevent instantiation

    public static <T> List<T> readList(Path path, Class<T> type) throws IOException {
        return mapper.readValue(Files.readAllBytes(path),
                mapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    public static void write(Path path, Object data) throws IOException {
        Path tempPath = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            mapper.writeValue(tempPath.toFile(), data);
            Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    public static <T> T readValue(Path path, Class<T> type) throws IOException {
        return mapper.readValue(path.toFile(), type);
    }
}
