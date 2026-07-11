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

    /**
     * Populated only on GET /api/badges and /api/banners responses (see
     * BadgeLibraryController/BannerLibraryController), which merge in
     * badges/banners owned by accepted partner streamers. Null/ignored on
     * create/update requests - who owns a badge is set server-side from the
     * authenticated streamer, never from the request body.
     */
    private Long ownerStreamerId;
    private boolean mine = true;
    private String ownerDisplayName;

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

    public Long getOwnerStreamerId() {
        return ownerStreamerId;
    }

    public void setOwnerStreamerId(Long ownerStreamerId) {
        this.ownerStreamerId = ownerStreamerId;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }
}
