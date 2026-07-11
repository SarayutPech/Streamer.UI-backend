package com.streamerui.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/** Local/single-tenant dev mode: there's no real account, just a stand-in. */
@Service
@Profile("!mysql")
public class FixedMeService implements MeService {
    @Override
    public MeResponse getMe(Long streamerId) {
        return new MeResponse(streamerId, "Local streamer (no login)", null, List.of());
    }
}
