package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data repository backing JpaBadgeLibraryRepository. */
public interface BadgeLibraryJpaRepository extends JpaRepository<BadgeLibraryEntity, String> {
    List<BadgeLibraryEntity> findAllByOwnerStreamerIdOrderBySortOrderAsc(Long ownerStreamerId);

    long countByOwnerStreamerId(Long ownerStreamerId);
}
