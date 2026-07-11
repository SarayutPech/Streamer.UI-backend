package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data repository backing JpaUserProfileRepository. */
public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, Long> {

    List<UserProfileEntity> findAllByOwnerStreamerId(Long ownerStreamerId);

    Optional<UserProfileEntity> findByOwnerStreamerIdAndChannelId(Long ownerStreamerId, String channelId);

    void deleteByOwnerStreamerIdAndChannelId(Long ownerStreamerId, String channelId);

    void deleteAllByOwnerStreamerId(Long ownerStreamerId);
}
