package com.streamerui.model;

import java.util.List;

/**
 * What gets pushed to the frontend over the /topic/chat WebSocket topic.
 * It's the raw YouTube message merged with the author's saved UserProfile
 * (badges, name color, glow, banner) so the overlay can render it directly
 * with no further lookups.
 */
public class ChatMessageDto {

    private String id;
    private String type; // textMessageEvent | superChatEvent | superStickerEvent | newSponsorEvent | membershipGiftingEvent
    private String publishedAt;
    private String channelId;
    private String displayName;
    private String profileImageUrl;
    private String messageText;
    private String amountDisplayString; // superchat/supersticker amount, if any

    // Merged from UserProfile
    private List<Badge> badges;
    private String nameColor;
    private boolean glowEnabled;
    private String glowColor;
    private int glowSize;
    private String bannerImageUrl;
    private boolean bannerEnabled;
    private String bannerMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getAmountDisplayString() {
        return amountDisplayString;
    }

    public void setAmountDisplayString(String amountDisplayString) {
        this.amountDisplayString = amountDisplayString;
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
