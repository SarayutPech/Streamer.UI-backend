package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreamerJpaRepository extends JpaRepository<StreamerEntity, Long> {
    List<StreamerEntity> findByDisplayNameContainingIgnoreCase(String query);

    Optional<StreamerEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
