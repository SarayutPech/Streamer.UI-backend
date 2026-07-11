package com.streamerui.security;

import com.streamerui.repository.jpa.OAuthIdentityEntity;
import com.streamerui.repository.jpa.OAuthIdentityJpaRepository;
import com.streamerui.repository.jpa.StreamerEntity;
import com.streamerui.repository.jpa.StreamerJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Finds or creates the Streamer + OAuthIdentity rows for an incoming OAuth2
 * login (see OAuthLoginSuccessHandler). Each (provider, providerUserId) maps
 * to exactly one streamer; the first time a given external account logs in,
 * a brand new Streamer row is created for it.
 */
@Service
@Profile("mysql")
public class StreamerAccountService {

    private final StreamerJpaRepository streamers;
    private final OAuthIdentityJpaRepository identities;

    public StreamerAccountService(StreamerJpaRepository streamers, OAuthIdentityJpaRepository identities) {
        this.streamers = streamers;
        this.identities = identities;
    }

    public static class LoginResult {
        public final Long streamerId;
        public final OAuthIdentityEntity identity;

        public LoginResult(Long streamerId, OAuthIdentityEntity identity) {
            this.streamerId = streamerId;
            this.identity = identity;
        }
    }

    @Transactional
    public LoginResult findOrCreateForLogin(OAuthIdentityEntity.Provider provider, String providerUserId,
                                             String email, String displayName, String avatarUrl) {
        OAuthIdentityEntity identity = identities.findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);

        if (identity != null) {
            identity.setEmail(email);
            identity.setDisplayName(displayName);
            identity.setAvatarUrl(avatarUrl);
            identity.setLastLoginAt(LocalDateTime.now());
            identities.save(identity);
            return new LoginResult(identity.getStreamerId(), identity);
        }

        StreamerEntity streamer = new StreamerEntity();
        streamer.setDisplayName(displayName != null ? displayName : "Streamer");
        streamer.setPrimaryEmail(email);
        streamer.setAvatarUrl(avatarUrl);
        streamer = streamers.save(streamer);

        OAuthIdentityEntity newIdentity = new OAuthIdentityEntity();
        newIdentity.setStreamerId(streamer.getId());
        newIdentity.setProvider(provider);
        newIdentity.setProviderUserId(providerUserId);
        newIdentity.setEmail(email);
        newIdentity.setDisplayName(displayName);
        newIdentity.setAvatarUrl(avatarUrl);
        newIdentity.setLastLoginAt(LocalDateTime.now());
        newIdentity = identities.save(newIdentity);

        return new LoginResult(streamer.getId(), newIdentity);
    }
}
