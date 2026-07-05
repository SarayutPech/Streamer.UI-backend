package com.streamerui.controller;

import com.streamerui.model.YoutubeConnectRequest;
import com.streamerui.model.YoutubeStatus;
import com.streamerui.service.YoutubeChatPollingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controls which YouTube live broadcast the overlay is currently pulling
 * chat messages from.
 */
@RestController
@RequestMapping("/api/youtube")
public class YoutubeController {

    private final YoutubeChatPollingService pollingService;

    public YoutubeController(YoutubeChatPollingService pollingService) {
        this.pollingService = pollingService;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@Valid @RequestBody YoutubeConnectRequest request) {
        try {
            YoutubeStatus status = pollingService.connect(request.getVideoUrlOrId());
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect() {
        pollingService.disconnect();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public YoutubeStatus status() {
        return pollingService.status();
    }
}
