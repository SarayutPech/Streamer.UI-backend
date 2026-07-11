package com.streamerui.repository.jpa;

import com.streamerui.model.OverlayConfig;
import com.streamerui.repository.OverlayConfigRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * OverlayConfigRepository backed by MySQL - one row per streamer (streamer_id
 * is the PK). Active only when the "mysql" Spring profile is on.
 */
@Repository
@Profile("mysql")
public class JpaOverlayConfigRepository implements OverlayConfigRepository {

    private final OverlayConfigJpaRepository jpa;

    public JpaOverlayConfigRepository(OverlayConfigJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OverlayConfig get(Long streamerId) {
        return jpa.findById(streamerId).map(JpaOverlayConfigRepository::toModel).orElseGet(OverlayConfig::new);
    }

    @Override
    @Transactional
    public OverlayConfig save(Long streamerId, OverlayConfig config) {
        OverlayConfigEntity entity = jpa.findById(streamerId).orElseGet(OverlayConfigEntity::new);
        entity.setStreamerId(streamerId);
        entity.setHeaderEnabled(config.isHeaderEnabled());
        entity.setHeaderText(config.getHeaderText());
        entity.setHeaderLogoUrl(config.getHeaderLogoUrl());
        entity.setHeaderBannerUrl(config.getHeaderBannerUrl());
        entity.setBackgroundType(config.getBackgroundType());
        entity.setBackgroundValue(config.getBackgroundValue());
        entity.setBadgeSlotCount(config.getBadgeSlotCount());
        entity.setDefaultGlowEnabled(config.isDefaultGlowEnabled());
        entity.setDefaultNameColor(config.getDefaultNameColor());
        entity.setFontFamily(config.getFontFamily());
        entity.setMaxMessages(config.getMaxMessages());
        entity.setBannerDisplaySeconds(config.getBannerDisplaySeconds());
        entity.setUserBannersEnabled(config.isUserBannersEnabled());
        jpa.save(entity);
        return config;
    }

    private static OverlayConfig toModel(OverlayConfigEntity entity) {
        OverlayConfig config = new OverlayConfig();
        config.setHeaderEnabled(entity.isHeaderEnabled());
        config.setHeaderText(entity.getHeaderText());
        config.setHeaderLogoUrl(entity.getHeaderLogoUrl());
        config.setHeaderBannerUrl(entity.getHeaderBannerUrl());
        config.setBackgroundType(entity.getBackgroundType());
        config.setBackgroundValue(entity.getBackgroundValue());
        config.setBadgeSlotCount(entity.getBadgeSlotCount());
        config.setDefaultGlowEnabled(entity.isDefaultGlowEnabled());
        config.setDefaultNameColor(entity.getDefaultNameColor());
        config.setFontFamily(entity.getFontFamily());
        config.setMaxMessages(entity.getMaxMessages());
        config.setBannerDisplaySeconds(entity.getBannerDisplaySeconds());
        config.setUserBannersEnabled(entity.isUserBannersEnabled());
        return config;
    }
}
