package com.streamerui.repository;

import com.streamerui.model.Badge;

import java.util.List;
import java.util.Optional;

/**
 * A shared catalog of reusable badge definitions (image + label), managed
 * once in the admin "Badge library" panel. Per-user badges (see
 * UserProfileRepository) are picked from this list instead of being
 * uploaded fresh every time.
 */
public interface BadgeLibraryRepository {

    List<Badge> findAll();

    Optional<Badge> findById(String id);

    Badge save(Badge badge);

    void deleteById(String id);
}
