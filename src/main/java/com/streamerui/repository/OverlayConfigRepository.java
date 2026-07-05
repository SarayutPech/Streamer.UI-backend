package com.streamerui.repository;

import com.streamerui.model.OverlayConfig;

/**
 * Storage-agnostic contract for the single global OverlayConfig record.
 * Backed by a JSON file today (see json.JsonOverlayConfigRepository);
 * swap to a DB-backed implementation later without touching callers.
 */
public interface OverlayConfigRepository {

    OverlayConfig get();

    OverlayConfig save(OverlayConfig config);
}
