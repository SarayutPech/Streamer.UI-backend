package com.streamerui.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamerui.config.StreamerUiProperties;
import com.streamerui.model.Badge;
import com.streamerui.repository.BannerLibraryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BannerLibraryRepository backed by data/banners.json. Mirrors
 * JsonBadgeLibraryRepository exactly. Local/single-tenant dev mode -
 * streamerId is accepted but ignored. Active by default; steps aside when
 * the "mysql" profile is on.
 */
@Repository
@Profile("!mysql")
public class JsonBannerLibraryRepository implements BannerLibraryRepository {

    private final JsonFileStore store;
    private final Path filePath;
    private final ConcurrentHashMap<String, Badge> cache = new ConcurrentHashMap<>();
    private final List<String> order = new ArrayList<>();

    public JsonBannerLibraryRepository(StreamerUiProperties properties, ObjectMapper mapper) {
        this.store = new JsonFileStore(mapper);
        this.filePath = Path.of(properties.getDataDir(), "banners.json");
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
    public synchronized List<Badge> findAll(Long streamerId) {
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
    public synchronized Optional<Badge> findById(Long streamerId, String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public synchronized Badge save(Long streamerId, Badge banner) {
        if (!cache.containsKey(banner.getId())) {
            order.add(banner.getId());
        }
        cache.put(banner.getId(), banner);
        persist();
        return banner;
    }

    @Override
    public synchronized void deleteById(Long streamerId, String id) {
        cache.remove(id);
        order.remove(id);
        persist();
    }
}
