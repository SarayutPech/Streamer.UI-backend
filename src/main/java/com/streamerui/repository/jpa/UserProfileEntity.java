package com.streamerui.repository.jpa;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL row for one chatter's saved appearance settings, scoped to the
 * streamer who owns it (owner_streamer_id). Mirrors
 * {@link com.streamerui.model.UserProfile}; see JpaUserProfileRepository for
 * the mapping between the two.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_streamer_id", nullable = false)
    private Long ownerStreamerId;

    @Column(name = "channel_id", length = 128, nullable = false)
    private String channelId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "name_color")
    private String nameColor = "#FFFFFF";

    @Column(name = "glow_enabled")
    private boolean glowEnabled = false;

    @Column(name = "glow_color")
    private String glowColor = "#7B61FF";

    @Column(name = "glow_size")
    private int glowSize = 10;

    @Column(name = "banner_image_url", length = 1024)
    private String bannerImageUrl;

    @Column(name = "banner_enabled")
    private boolean bannerEnabled = false;

    @Column(name = "banner_message", length = 2048)
    private String bannerMessage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_profile_badges", joinColumns = @JoinColumn(name = "user_profile_id"))
    @OrderColumn(name = "position")
    private List<BadgeEmbeddable> badges = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerStreamerId() {
        return ownerStreamerId;
    }

    public void setOwnerStreamerId(Long ownerStreamerId) {
        this.ownerStreamerId = ownerStreamerId;
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

    public List<BadgeEmbeddable> getBadges() {
        return badges;
    }

    public void setBadges(List<BadgeEmbeddable> badges) {
        this.badges = badges;
    }
}
