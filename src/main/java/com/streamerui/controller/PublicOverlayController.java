package com.streamerui.controller;

import com.streamerui.model.OverlayConfig;
import com.streamerui.repository.OverlayConfigRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unauthenticated read-only endpoint for the overlay page itself (loaded as
 * an OBS browser source, which can't log in). The overlay URL embeds the
 * streamer id, e.g. https://.../overlay/42 - the frontend calls this once on
 * load to paint the initial header/background before the WebSocket
 * connection (/topic/config/{streamerId}) takes over for live updates.
 *
 * Always permitAll (see SecurityConfig - /api/public/** is open in both
 * local and production security chains).
 */
@RestController
@RequestMapping("/api/public")
public class PublicOverlayController {

    private final OverlayConfigRepository overlayConfigRepository;

    public PublicOverlayController(OverlayConfigRepository overlayConfigRepository) {
        this.overlayConfigRepository = overlayConfigRepository;
    }

    @GetMapping("/{streamerId}/config")
    public OverlayConfig config(@PathVariable Long streamerId) {
        return overlayConfigRepository.get(streamerId);
    }
}
