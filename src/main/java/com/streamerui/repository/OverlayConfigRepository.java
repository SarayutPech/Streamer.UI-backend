package com.streamerui.repository;

import com.streamerui.model.OverlayConfig;

/**
 * Storage-agnostic contract for one streamer's OverlayConfig record.
 * Backed by a JSON file locally (single-tenant, ignores streamerId - see
 * json.JsonOverlayConfigRepository) or by MySQL in production, one row per
 * streamer (see jpa.JpaOverlayConfigRepository).
 */
public interface OverlayConfigRepository {

    OverlayConfig get(Long streamerId);

    OverlayConfig save(Long streamerId, OverlayConfig config);
}
