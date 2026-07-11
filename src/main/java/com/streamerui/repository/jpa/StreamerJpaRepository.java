package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreamerJpaRepository extends JpaRepository<StreamerEntity, Long> {
    List<StreamerEntity> findByDisplayNameContainingIgnoreCase(String query);
}
