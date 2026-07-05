package com.streamerui.model;

/**
 * A single badge shown after a user's name in chat (e.g. VIP, Moderator, Sub tier).
 * A user can have several; only the first {@code badgeSlotCount} (from OverlayConfig)
 * are actually rendered, in list order.
 */
public class Badge {

    private String id;
    private String label;
    private String imageUrl;

    public Badge() {
    }

    public Badge(String id, String label, String imageUrl) {
        this.id = id;
        this.label = label;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
