package com.streamerui.repository;

import com.streamerui.model.Badge;

import java.util.List;
import java.util.Optional;

/**
 * A shared catalog of reusable badge definitions (image + label), managed
 * once in the admin "Badge library" panel, scoped to the streamer who owns
 * them. Per-user badges (see UserProfileRepository) are picked from this
 * list instead of being uploaded fresh every time.
 *
 * Note: "streamer X can also see streamer Y's badges once partnered" is
 * handled by the caller (composing findAll(me) with findAll(partnerId) for
 * each accepted partner) - not by this repository.
 */
public interface BadgeLibraryRepository {

    List<Badge> findAll(Long streamerId);

    Optional<Badge> findById(Long streamerId, String id);

    Badge save(Long streamerId, Badge badge);

    void deleteById(Long streamerId, String id);
}
