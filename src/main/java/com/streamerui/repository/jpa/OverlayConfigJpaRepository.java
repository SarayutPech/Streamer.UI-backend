package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository backing JpaOverlayConfigRepository. */
public interface OverlayConfigJpaRepository extends JpaRepository<OverlayConfigEntity, Long> {
}
