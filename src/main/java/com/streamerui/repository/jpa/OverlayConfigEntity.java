package com.streamerui.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Overlay config, one row per streamer (streamer_id is both the PK and a FK
 * to streamers.id - see JpaOverlayConfigRepository).
 */
@Entity
@Table(name = "overlay_config")
public class OverlayConfigEntity {

    @Id
    @Column(name = "streamer_id")
    private Long streamerId;

    private boolean headerEnabled = true;

    @Column(length = 512)
    private String headerText = "Live Chat";

    @Column(length = 1024)
    private String headerLogoUrl;

    @Column(length = 1024)
    private String headerBannerUrl;

    private String backgroundType = "color";

    @Column(length = 512)
    private String backgroundValue = "#0f0f13cc";

    private int badgeSlotCount = 4;

    private boolean defaultGlowEnabled = false;

    private String defaultNameColor = "#FFFFFF";

    @Column(length = 512)
    private String fontFamily = "'Inter', 'Noto Sans Thai', sans-serif";

    private int maxMessages = 60;

    private int bannerDisplaySeconds = 6;

    private boolean userBannersEnabled = true;

    public Long getStreamerId() {
        return streamerId;
    }

    public void setStreamerId(Long streamerId) {
        this.streamerId = streamerId;
    }

    public boolean isHeaderEnabled() {
        return headerEnabled;
    }

    public void setHeaderEnabled(boolean headerEnabled) {
        this.headerEnabled = headerEnabled;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public String getHeaderLogoUrl() {
        return headerLogoUrl;
    }

    public void setHeaderLogoUrl(String headerLogoUrl) {
        this.headerLogoUrl = headerLogoUrl;
    }

    public String getHeaderBannerUrl() {
        return headerBannerUrl;
    }

    public void setHeaderBannerUrl(String headerBannerUrl) {
        this.headerBannerUrl = headerBannerUrl;
    }

    public String getBackgroundType() {
        return backgroundType;
    }

    public void setBackgroundType(String backgroundType) {
        this.backgroundType = backgroundType;
    }

    public String getBackgroundValue() {
        return backgroundValue;
    }

    public void setBackgroundValue(String backgroundValue) {
        this.backgroundValue = backgroundValue;
    }

    public int getBadgeSlotCount() {
        return badgeSlotCount;
    }

    public void setBadgeSlotCount(int badgeSlotCount) {
        this.badgeSlotCount = badgeSlotCount;
    }

    public boolean isDefaultGlowEnabled() {
        return defaultGlowEnabled;
    }

    public void setDefaultGlowEnabled(boolean defaultGlowEnabled) {
        this.defaultGlowEnabled = defaultGlowEnabled;
    }

    public String getDefaultNameColor() {
        return defaultNameColor;
    }

    public void setDefaultNameColor(String defaultNameColor) {
        this.defaultNameColor = defaultNameColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public int getBannerDisplaySeconds() {
        return bannerDisplaySeconds;
    }

    public void setBannerDisplaySeconds(int bannerDisplaySeconds) {
        this.bannerDisplaySeconds = bannerDisplaySeconds;
    }

    public boolean isUserBannersEnabled() {
        return userBannersEnabled;
    }

    public void setUserBannersEnabled(boolean userBannersEnabled) {
        this.userBannersEnabled = userBannersEnabled;
    }
}
