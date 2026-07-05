package com.streamerui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-user chat appearance settings, keyed by the YouTube channelId of the author.
 * This is what currently lives in data/users.json (see JsonUserProfileRepository).
 * Swapping to a real database later only requires a new UserProfileRepository
 * implementation - nothing else in the app depends on the storage format.
 */
public class UserProfile {

    private String channelId;
    private String displayName;
    private List<Badge> badges = new ArrayList<>();
    private String nameColor = "#FFFFFF";
    private boolean glowEnabled = false;
    private String glowColor = "#7B61FF";
    private int glowSize = 10; // px, roughly 4-24 makes sense; kept low to avoid the blocky look large text-shadow blur causes
    private String bannerImageUrl;
    private boolean bannerEnabled = false;
    private String bannerMessage;

    public UserProfile() {
    }

    public UserProfile(String channelId, String displayName) {
        this.channelId = channelId;
        this.displayName = displayName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public void setBadges(List<Badge> badges) {
        this.badges = badges;
    }

    public String getNameColor() {
        return nameColor;
    }

    public void setNameColor(String nameColor) {
        this.nameColor = nameColor;
    }

    public boolean isGlowEnabled() {
        return glowEnabled;
    }

    public void setGlowEnabled(boolean glowEnabled) {
        this.glowEnabled = glowEnabled;
    }

    public String getGlowColor() {
        return glowColor;
    }

    public void setGlowColor(String glowColor) {
        this.glowColor = glowColor;
    }

    public int getGlowSize() {
        return glowSize;
    }

    public void setGlowSize(int glowSize) {
        this.glowSize = glowSize;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public void setBannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
    }

    public String getBannerMessage() {
        return bannerMessage;
    }

    public void setBannerMessage(String bannerMessage) {
        this.bannerMessage = bannerMessage;
    }
}
