package com.streamerui.controller;

import com.streamerui.model.OverlayConfig;
import com.streamerui.repository.OverlayConfigRepository;
import com.streamerui.security.CurrentStreamerService;
import com.streamerui.service.ChatBroadcastService;
import org.springframework.web.bind.annotation.*;

/**
 * The current streamer's overlay settings: header, background, badge slot
 * count, default name styling. Saving pushes the new config to that
 * streamer's connected overlay pages over WebSocket so changes apply live
 * without reloading OBS.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final OverlayConfigRepository repository;
    private final ChatBroadcastService broadcastService;
    private final CurrentStreamerService currentStreamer;

    public ConfigController(OverlayConfigRepository repository, ChatBroadcastService broadcastService,
                             CurrentStreamerService currentStreamer) {
        this.repository = repository;
        this.broadcastService = broadcastService;
        this.currentStreamer = currentStreamer;
    }

    @GetMapping
    public OverlayConfig get() {
        return repository.get(currentStreamer.requireStreamerId());
    }

    @PutMapping
    public OverlayConfig update(@RequestBody OverlayConfig config) {
        Long streamerId = currentStreamer.requireStreamerId();
        OverlayConfig saved = repository.save(streamerId, config);
        broadcastService.broadcastConfig(streamerId, saved);
        return saved;
    }
}
