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
 * The shared "partnership profile" for one chatter, visible/editable by
 * both streamers in an accepted partnership. Independent of a streamer's
 * own UserProfileEntity for the same channel_id - the app decides which
 * one wins when both exist (see the example query in
 * schema/streamerui_multi_tenant.sql).
 */
@Entity
@Table(name = "partnership_user_profiles")
public class PartnershipUserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partnership_id", nullable = false)
    private Long partnershipId;

    @Column(name = "channel_id", length = 128, nullable = false)
    private String channelId;

    @Column(name = "display_name", length = 256)
    private String displayName;

    @Column(name = "name_color", length = 16)
    private String nameColor = "#FFFFFF";

    @Column(name = "glow_enabled")
    private boolean glowEnabled = false;

    @Column(name = "glow_color", length = 16)
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
    @CollectionTable(name = "partnership_user_profile_badges", joinColumns = @JoinColumn(name = "partnership_user_profile_id"))
    @OrderColumn(name = "position")
    private List<BadgeEmbeddable> badges = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartnershipId() {
        return partnershipId;
    }

    public void setPartnershipId(Long partnershipId) {
        this.partnershipId = partnershipId;
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
