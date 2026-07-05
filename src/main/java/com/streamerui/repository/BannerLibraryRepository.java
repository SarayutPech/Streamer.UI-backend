package com.streamerui.repository;

import com.streamerui.model.Badge;

import java.util.List;
import java.util.Optional;

/**
 * A shared catalog of reusable banner images (same shape as BadgeLibraryRepository:
 * id/label/imageUrl), managed once in the admin "Banner library" panel. Per-user
 * banners (see UserProfileRepository) are picked from this list instead of being
 * uploaded fresh for every chatter.
 */
public interface BannerLibraryRepository {

    List<Badge> findAll();

    Optional<Badge> findById(String id);

    Badge save(Badge banner);

    void deleteById(String id);
}
