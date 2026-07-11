package com.streamerui.security;

import com.streamerui.repository.jpa.OAuthIdentityJpaRepository;
import com.streamerui.repository.jpa.StreamerEntity;
import com.streamerui.repository.jpa.StreamerJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("mysql")
public class JpaMeService implements MeService {

    private final StreamerJpaRepository streamers;
    private final OAuthIdentityJpaRepository identities;

    public JpaMeService(StreamerJpaRepository streamers, OAuthIdentityJpaRepository identities) {
        this.streamers = streamers;
        this.identities = identities;
    }

    @Override
    public MeResponse getMe(Long streamerId) {
        StreamerEntity streamer = streamers.findById(streamerId)
                .orElseThrow(() -> new IllegalStateException("Streamer not found: " + streamerId));
        List<String> providers = identities.findByStreamerId(streamerId).stream()
                .map(i -> i.getProvider().name())
                .collect(Collectors.toList());
        return new MeResponse(streamer.getId(), streamer.getDisplayName(), streamer.getAvatarUrl(), providers);
    }
}
