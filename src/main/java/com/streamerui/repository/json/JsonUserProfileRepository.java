package com.streamerui.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamerui.config.StreamerUiProperties;
import com.streamerui.model.UserProfile;
import com.streamerui.repository.UserProfileRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserProfileRepository backed by data/users.json. The whole list is kept in
 * an in-memory map for fast lookups and re-written to disk on every mutation.
 * Fine for a single-streamer overlay with a modest chatter count; swap for a
 * DB-backed implementation if that stops being true.
 */
@Repository
public class JsonUserProfileRepository implements UserProfileRepository {

    private final JsonFileStore store;
    private final Path filePath;
    private final ConcurrentHashMap<String, UserProfile> cache = new ConcurrentHashMap<>();

    public JsonUserProfileRepository(StreamerUiProperties properties, ObjectMapper mapper) {
        this.store = new JsonFileStore(mapper);
        this.filePath = Path.of(properties.getDataDir(), "users.json");
        load();
    }

    private void load() {
        List<UserProfile> all = store.readList(filePath, UserProfile.class);
        cache.clear();
        for (UserProfile p : all) {
            cache.put(p.getChannelId(), p);
        }
    }

    private void persist() {
        store.writeList(filePath, new ArrayList<>(cache.values()));
    }

    @Override
    public synchronized List<UserProfile> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized Optional<UserProfile> findByChannelId(String channelId) {
        return Optional.ofNullable(cache.get(channelId));
    }

    @Override
    public synchronized UserProfile save(UserProfile profile) {
        cache.put(profile.getChannelId(), profile);
        persist();
        return profile;
    }

    @Override
    public synchronized void deleteByChannelId(String channelId) {
        cache.remove(channelId);
        persist();
    }

    @Override
    public synchronized void deleteAll() {
        cache.clear();
        persist();
    }

    @Override
    public synchronized UserProfile findOrCreate(String channelId, String displayName) {
        UserProfile existing = cache.get(channelId);
        if (existing != null) {
            return existing;
        }
        UserProfile created = new UserProfile(channelId, displayName);
        cache.put(channelId, created);
        persist();
        return created;
    }
}
