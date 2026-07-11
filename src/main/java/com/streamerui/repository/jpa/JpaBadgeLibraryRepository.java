package com.streamerui.repository.jpa;

import com.streamerui.model.Badge;
import com.streamerui.repository.BadgeLibraryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BadgeLibraryRepository backed by MySQL, scoped by owner_streamer_id.
 * Active only when the "mysql" Spring profile is on.
 */
@Repository
@Profile("mysql")
public class JpaBadgeLibraryRepository implements BadgeLibraryRepository {

    private final BadgeLibraryJpaRepository jpa;

    public JpaBadgeLibraryRepository(BadgeLibraryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Badge> findAll(Long streamerId) {
        return jpa.findAllByOwnerStreamerIdOrderBySortOrderAsc(streamerId).stream()
                .map(JpaBadgeLibraryRepository::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Badge> findById(Long streamerId, String id) {
        return jpa.findById(id)
                .filter(e -> e.getOwnerStreamerId().equals(streamerId))
                .map(JpaBadgeLibraryRepository::toModel);
    }

    @Override
    @Transactional
    public Badge save(Long streamerId, Badge badge) {
        BadgeLibraryEntity entity = jpa.findById(badge.getId())
                .filter(e -> e.getOwnerStreamerId().equals(streamerId))
                .orElseGet(() -> {
                    BadgeLibraryEntity e = new BadgeLibraryEntity();
                    e.setId(badge.getId());
                    e.setOwnerStreamerId(streamerId);
                    e.setSortOrder(jpa.countByOwnerStreamerId(streamerId));
                    return e;
                });
        entity.setLabel(badge.getLabel());
        entity.setImageUrl(badge.getImageUrl());
        jpa.save(entity);
        return badge;
    }

    @Override
    @Transactional
    public void deleteById(Long streamerId, String id) {
        jpa.findById(id)
                .filter(e -> e.getOwnerStreamerId().equals(streamerId))
                .ifPresent(e -> jpa.deleteById(e.getId()));
    }

    private static Badge toModel(BadgeLibraryEntity entity) {
        return new Badge(entity.getId(), entity.getLabel(), entity.getImageUrl());
    }
}
