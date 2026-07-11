package com.streamerui.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Local/single-tenant dev mode: there's no login, so every request is
 * treated as the same implicit local "streamer" (matches the old
 * single-tenant behavior - one JSON data store, no accounts).
 */
@Service
@Profile("!mysql")
public class FixedCurrentStreamerService implements CurrentStreamerService {

    /** Arbitrary constant - meaningless in local/JSON mode, just needs to be stable. */
    public static final Long LOCAL_STREAMER_ID = 1L;

    @Override
    public Long requireStreamerId() {
        return LOCAL_STREAMER_ID;
    }
}
