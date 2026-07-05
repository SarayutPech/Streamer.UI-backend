package com.streamerui.controller;

import com.streamerui.model.OverlayConfig;
import com.streamerui.repository.OverlayConfigRepository;
import com.streamerui.service.ChatBroadcastService;
import org.springframework.web.bind.annotation.*;

/**
 * Global overlay settings: header, background, badge slot count, default
 * name styling. Saving pushes the new config to any connected overlay pages
 * over WebSocket so changes apply live without reloading OBS.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final OverlayConfigRepository repository;
    private final ChatBroadcastService broadcastService;

    public ConfigController(OverlayConfigRepository repository, ChatBroadcastService broadcastService) {
        this.repository = repository;
        this.broadcastService = broadcastService;
    }

    @GetMapping
    public OverlayConfig get() {
        return repository.get();
    }

    @PutMapping
    public OverlayConfig update(@RequestBody OverlayConfig config) {
        OverlayConfig saved = repository.save(config);
        broadcastService.broadcastConfig(saved);
        return saved;
    }
}
