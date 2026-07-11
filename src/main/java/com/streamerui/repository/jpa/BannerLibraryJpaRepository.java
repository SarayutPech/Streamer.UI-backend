package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data repository backing JpaBannerLibraryRepository. */
public interface BannerLibraryJpaRepository extends JpaRepository<BannerLibraryEntity, String> {
    List<BannerLibraryEntity> findAllByOwnerStreamerIdOrderBySortOrderAsc(Long ownerStreamerId);

    long countByOwnerStreamerId(Long ownerStreamerId);
}
