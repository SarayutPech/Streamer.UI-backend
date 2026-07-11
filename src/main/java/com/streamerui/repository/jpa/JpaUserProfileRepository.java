package com.streamerui.repository.jpa;

import com.streamerui.model.Badge;
import com.streamerui.model.UserProfile;
import com.streamerui.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserProfileRepository backed by MySQL (via Spring Data JPA), scoped by
 * owner_streamer_id. Active only when the "mysql" Spring profile is on (see
 * application-prod.yml); the JSON-file implementation stays the default
 * otherwise.
 */
@Repository
@Profile("mysql")
public class JpaUserProfileRepository implements UserProfileRepository {

    private final UserProfileJpaRepository jpa;

    public JpaUserProfileRepository(UserProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<UserProfile> findAll(Long streamerId) {
        return jpa.findAllByOwnerStreamerId(streamerId).stream()
                .map(JpaUserProfileRepository::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserProfile> findByChannelId(Long streamerId, String channelId) {
        return jpa.findByOwnerStreamerIdAndChannelId(streamerId, channelId).map(JpaUserProfileRepository::toModel);
    }

    @Override
    @Transactional
    public UserProfile save(Long streamerId, UserProfile profile) {
        UserProfileEntity entity = jpa.findByOwnerStreamerIdAndChannelId(streamerId, profile.getChannelId())
                .orElseGet(UserProfileEntity::new);
        entity.setOwnerStreamerId(streamerId);
        toEntity(profile, entity);
        return toModel(jpa.save(entity));
    }

    @Override
    @Transactional
    public void deleteByChannelId(Long streamerId, String channelId) {
        jpa.deleteByOwnerStreamerIdAndChannelId(streamerId, channelId);
    }

    @Override
    @Transactional
    public void deleteAll(Long streamerId) {
        jpa.deleteAllByOwnerStreamerId(streamerId);
    }

    @Override
    @Transactional
    public UserProfile findOrCreate(Long streamerId, String channelId, String displayName) {
        return jpa.findByOwnerStreamerIdAndChannelId(streamerId, channelId)
                .map(JpaUserProfileRepository::toModel)
                .orElseGet(() -> save(streamerId, new UserProfile(channelId, displayName)));
    }

    private static void toEntity(UserProfile profile, UserProfileEntity entity) {
        entity.setChannelId(profile.getChannelId());
        entity.setDisplayName(profile.getDisplayName());
        entity.setNameColor(profile.getNameColor());
        entity.setGlowEnabled(profile.isGlowEnabled());
        entity.setGlowColor(profile.getGlowColor());
        entity.setGlowSize(profile.getGlowSize());
        entity.setBannerImageUrl(profile.getBannerImageUrl());
        entity.setBannerEnabled(profile.isBannerEnabled());
        entity.setBannerMessage(profile.getBannerMessage());
        List<BadgeEmbeddable> badges = new ArrayList<>();
        if (profile.getBadges() != null) {
            for (Badge b : profile.getBadges()) {
                badges.add(new BadgeEmbeddable(b.getId(), b.getLabel(), b.getImageUrl()));
            }
        }
        entity.setBadges(badges);
    }

    private static UserProfile toModel(UserProfileEntity entity) {
        UserProfile profile = new UserProfile(entity.getChannelId(), entity.getDisplayName());
        profile.setNameColor(entity.getNameColor());
        profile.setGlowEnabled(entity.isGlowEnabled());
        profile.setGlowColor(entity.getGlowColor());
        profile.setGlowSize(entity.getGlowSize());
        profile.setBannerImageUrl(entity.getBannerImageUrl());
        profile.setBannerEnabled(entity.isBannerEnabled());
        profile.setBannerMessage(entity.getBannerMessage());
        List<Badge> badges = new ArrayList<>();
        for (BadgeEmbeddable b : entity.getBadges()) {
            badges.add(new Badge(b.getBadgeId(), b.getLabel(), b.getImageUrl()));
        }
        profile.setBadges(badges);
        return profile;
    }
}
