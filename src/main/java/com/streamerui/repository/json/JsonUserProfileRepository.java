package com.streamerui.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamerui.config.StreamerUiProperties;
import com.streamerui.model.UserProfile;
import com.streamerui.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;
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
 *
 * This is the local/single-tenant dev mode - the streamerId argument on
 * every method is accepted (to match the multi-tenant UserProfileRepository
 * contract) but ignored, since there's only ever one implicit local
 * "streamer" here. Active by default; steps aside when the "mysql" profile
 * is on (see com.streamerui.repository.jpa.JpaUserProfileRepository).
 */
@Repository
@Profile("!mysql")
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
    public synchronized List<UserProfile> findAll(Long streamerId) {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized Optional<UserProfile> findByChannelId(Long streamerId, String channelId) {
        return Optional.ofNullable(cache.get(channelId));
    }

    @Override
    public synchronized UserProfile save(Long streamerId, UserProfile profile) {
        cache.put(profile.getChannelId(), profile);
        persist();
        return profile;
    }

    @Override
    public synchronized void deleteByChannelId(Long streamerId, String channelId) {
        cache.remove(channelId);
        persist();
    }

    @Override
    public synchronized void deleteAll(Long streamerId) {
        cache.clear();
        persist();
    }

    @Override
    public synchronized UserProfile findOrCreate(Long streamerId, String channelId, String displayName) {
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
