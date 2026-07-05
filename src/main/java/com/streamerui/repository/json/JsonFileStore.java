package com.streamerui.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal helper for reading/writing a JSON "text file" database. Writes are
 * atomic (write to a temp file, then move-replace) so a crash mid-write can't
 * corrupt the data file. All access goes through a lock object per-instance
 * so concurrent requests don't interleave writes.
 *
 * This is intentionally simple - it's the "DB" for now. Once real persistence
 * is needed, replace the repository implementations that use this class with
 * JPA/DB-backed ones; nothing else in the app touches this class directly.
 */
public class JsonFileStore {

    private final ObjectMapper mapper;
    private final Object lock = new Object();

    public JsonFileStore(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> List<T> readList(Path path, Class<T> elementType) {
        synchronized (lock) {
            try {
                if (!Files.exists(path)) {
                    return new ArrayList<>();
                }
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
                List<T> result = mapper.readValue(path.toFile(), listType);
                return result != null ? result : new ArrayList<>();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read JSON store at " + path, e);
            }
        }
    }

    public <T> void writeList(Path path, List<T> items) {
        synchronized (lock) {
            writeAtomic(path, items);
        }
    }

    public <T> T readObject(Path path, Class<T> type, T defaultValue) {
        synchronized (lock) {
            try {
                if (!Files.exists(path)) {
                    return defaultValue;
                }
                T result = mapper.readValue(path.toFile(), type);
                return result != null ? result : defaultValue;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read JSON store at " + path, e);
            }
        }
    }

    public <T> void writeObject(Path path, T value) {
        synchronized (lock) {
            writeAtomic(path, value);
        }
    }

    private void writeAtomic(Path path, Object value) {
        try {
            File parentDir = path.toAbsolutePath().getParent().toFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            Path tmp = Path.of(path.toAbsolutePath() + ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), value);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write JSON store at " + path, e);
        }
    }
}
