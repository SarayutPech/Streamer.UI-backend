package com.streamerui.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** MySQL row for one banner-library entry. Mirrors BadgeLibraryEntity, separate table. */
@Entity
@Table(name = "banner_library")
public class BannerLibraryEntity {

    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "owner_streamer_id", nullable = false)
    private Long ownerStreamerId;

    @Column(length = 256)
    private String label;

    @Column(length = 1024)
    private String imageUrl;

    /** Preserves insertion order for findAll(), since ids aren't sequential. */
    private long sortOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getOwnerStreamerId() {
        return ownerStreamerId;
    }

    public void setOwnerStreamerId(Long ownerStreamerId) {
        this.ownerStreamerId = ownerStreamerId;
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

    public long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(long sortOrder) {
        this.sortOrder = sortOrder;
    }
}
