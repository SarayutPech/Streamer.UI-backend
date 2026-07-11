package com.streamerui.repository.jpa;

import com.streamerui.model.Badge;
import com.streamerui.repository.BannerLibraryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BannerLibraryRepository backed by MySQL, scoped by owner_streamer_id.
 * Active only when the "mysql" Spring profile is on.
 */
@Repository
@Profile("mysql")
public class JpaBannerLibraryRepository implements BannerLibraryRepository {

    private final BannerLibraryJpaRepository jpa;

    public JpaBannerLibraryRepository(BannerLibraryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Badge> findAll(Long streamerId) {
        return jpa.findAllByOwnerStreamerIdOrderBySortOrderAsc(streamerId).stream()
                .map(JpaBannerLibraryRepository::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Badge> findById(Long streamerId, String id) {
        return jpa.findById(id)
                .filter(e -> e.getOwnerStreamerId().equals(streamerId))
                .map(JpaBannerLibraryRepository::toModel);
    }

    @Override
    @Transactional
    public Badge save(Long streamerId, Badge banner) {
        BannerLibraryEntity entity = jpa.findById(banner.getId())
                .filter(e -> e.getOwnerStreamerId().equals(streamerId))
                .orElseGet(() -> {
                    BannerLibraryEntity e = new BannerLibraryEntity();
                    e.setId(banner.getId());
                    e.setOwnerStreamerId(streamerId);
                    e.setSortOrder(jpa.countByOwnerStreamerId(streamerId));
                    return e;
                });
        entity.setLabel(banner.getLabel());
        entity.setImageUrl(banner.getImageUrl());
        jpa.save(entity);
        return banner;
    }

    @Override
    @Transactional
    public void deleteById(Long streamerId, String id) {
        jpa.findById(id)
                .filter(e -> e.getOwnerStreamerId().equals(streamerId))
                .ifPresent(e -> jpa.deleteById(e.getId()));
    }

    private static Badge toModel(BannerLibraryEntity entity) {
        return new Badge(entity.getId(), entity.getLabel(), entity.getImageUrl());
    }
}
