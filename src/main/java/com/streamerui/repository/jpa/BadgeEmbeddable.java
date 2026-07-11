package com.streamerui.repository.jpa;

import jakarta.persistence.Embeddable;

/**
 * JPA-embeddable copy of {@link com.streamerui.model.Badge}, stored inline in
 * the user_profile_badges / library tables (see UserProfileEntity,
 * BadgeLibraryEntity, BannerLibraryEntity). Kept separate from the domain
 * Badge class so persistence annotations don't leak into model/ .
 */
@Embeddable
public class BadgeEmbeddable {

    private String badgeId;
    private String label;
    private String imageUrl;

    public BadgeEmbeddable() {
    }

    public BadgeEmbeddable(String badgeId, String label, String imageUrl) {
        this.badgeId = badgeId;
        this.label = label;
        this.imageUrl = imageUrl;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
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
