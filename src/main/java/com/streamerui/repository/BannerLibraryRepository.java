package com.streamerui.repository;

import com.streamerui.model.Badge;

import java.util.List;
import java.util.Optional;

/**
 * A shared catalog of reusable banner images (same shape as
 * BadgeLibraryRepository: id/label/imageUrl), scoped to the streamer who
 * owns them. See BadgeLibraryRepository for the partner-visibility note.
 */
public interface BannerLibraryRepository {

    List<Badge> findAll(Long streamerId);

    Optional<Badge> findById(Long streamerId, String id);

    Badge save(Long streamerId, Badge banner);

    void deleteById(Long streamerId, String id);
}
