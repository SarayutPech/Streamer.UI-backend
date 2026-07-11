package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthIdentityJpaRepository extends JpaRepository<OAuthIdentityEntity, Long> {
    Optional<OAuthIdentityEntity> findByProviderAndProviderUserId(OAuthIdentityEntity.Provider provider, String providerUserId);

    List<OAuthIdentityEntity> findByStreamerId(Long streamerId);
}
