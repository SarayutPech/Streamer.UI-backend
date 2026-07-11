package com.streamerui.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamerui.config.StreamerUiProperties;
import com.streamerui.model.OverlayConfig;
import com.streamerui.repository.OverlayConfigRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

/**
 * OverlayConfigRepository backed by data/config.json - a single record file.
 * Local/single-tenant dev mode: streamerId is accepted but ignored (there's
 * only ever one implicit local "streamer"). Active by default; steps aside
 * when the "mysql" profile is on.
 */
@Repository
@Profile("!mysql")
public class JsonOverlayConfigRepository implements OverlayConfigRepository {

    private final JsonFileStore store;
    private final Path filePath;
    private volatile OverlayConfig cached;

    public JsonOverlayConfigRepository(StreamerUiProperties properties, ObjectMapper mapper) {
        this.store = new JsonFileStore(mapper);
        this.filePath = Path.of(properties.getDataDir(), "config.json");
        this.cached = store.readObject(filePath, OverlayConfig.class, new OverlayConfig());
    }

    @Override
    public synchronized OverlayConfig get(Long streamerId) {
        return cached;
    }

    @Override
    public synchronized OverlayConfig save(Long streamerId, OverlayConfig config) {
        store.writeObject(filePath, config);
        this.cached = config;
        return config;
    }
}
