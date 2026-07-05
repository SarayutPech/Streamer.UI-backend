package com.streamerui.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamerui.config.StreamerUiProperties;
import com.streamerui.model.Badge;
import com.streamerui.repository.BadgeLibraryRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BadgeLibraryRepository backed by data/badges.json.
 */
@Repository
public class JsonBadgeLibraryRepository implements BadgeLibraryRepository {

    private final JsonFileStore store;
    private final Path filePath;
    private final ConcurrentHashMap<String, Badge> cache = new ConcurrentHashMap<>();
    private final List<String> order = new ArrayList<>();

    public JsonBadgeLibraryRepository(StreamerUiProperties properties, ObjectMapper mapper) {
        this.store = new JsonFileStore(mapper);
        this.filePath = Path.of(properties.getDataDir(), "badges.json");
        load();
    }

    private synchronized void load() {
        List<Badge> all = store.readList(filePath, Badge.class);
        cache.clear();
        order.clear();
        for (Badge b : all) {
            cache.put(b.getId(), b);
            order.add(b.getId());
        }
    }

    private synchronized void persist() {
        List<Badge> all = new ArrayList<>();
        for (String id : order) {
            Badge b = cache.get(id);
            if (b != null) {
                all.add(b);
            }
        }
        store.writeList(filePath, all);
    }

    @Override
    public synchronized List<Badge> findAll() {
        List<Badge> all = new ArrayList<>();
        for (String id : order) {
            Badge b = cache.get(id);
            if (b != null) {
                all.add(b);
            }
        }
        return all;
    }

    @Override
    public synchronized Optional<Badge> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public synchronized Badge save(Badge badge) {
        if (!cache.containsKey(badge.getId())) {
            order.add(badge.getId());
        }
        cache.put(badge.getId(), badge);
        persist();
        return badge;
    }

    @Override
    public synchronized void deleteById(String id) {
        cache.remove(id);
        order.remove(id);
        persist();
    }
}
