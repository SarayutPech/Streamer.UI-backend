package com.streamerui.model;

/**
 * Body for POST /api/preview - lets the admin UI push a fake chat message
 * through the exact same enrichment pipeline as a real one, so badges/
 * glow/banner/event styling can be tested without a live YouTube chat.
 */
public class PreviewRequest {

    /** Optional. If it matches (or should become) a saved profile, reuse/create it. */
    private String channelId;

    /** Optional display name; used to create a profile if channelId is new. */
    private String displayName;

    /** textMessageEvent (default) | superChatEvent | superStickerEvent | newSponsorEvent */
    private String type;

    private String messageText;

    /** Shown as the super chat / super sticker amount, if relevant. */
    private String amountDisplayString;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
