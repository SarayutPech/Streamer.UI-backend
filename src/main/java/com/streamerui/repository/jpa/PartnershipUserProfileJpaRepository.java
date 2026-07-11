package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnershipUserProfileJpaRepository extends JpaRepository<PartnershipUserProfileEntity, Long> {
    Optional<PartnershipUserProfileEntity> findByPartnershipIdAndChannelId(Long partnershipId, String channelId);
}
