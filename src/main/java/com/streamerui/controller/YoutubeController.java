package com.streamerui.controller;

import com.streamerui.model.YoutubeConnectRequest;
import com.streamerui.model.YoutubeStatus;
import com.streamerui.security.CurrentStreamerService;
import com.streamerui.service.YoutubeChatPollingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controls which YouTube live broadcast the current streamer's overlay is
 * pulling chat messages from.
 */
@RestController
@RequestMapping("/api/youtube")
public class YoutubeController {

    private final YoutubeChatPollingService pollingService;
    private final CurrentStreamerService currentStreamer;

    public YoutubeController(YoutubeChatPollingService pollingService, CurrentStreamerService currentStreamer) {
        this.pollingService = pollingService;
        this.currentStreamer = currentStreamer;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@Valid @RequestBody YoutubeConnectRequest request) {
        try {
            YoutubeStatus status = pollingService.connect(currentStreamer.requireStreamerId(), request.getVideoUrlOrId());
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect() {
        pollingService.disconnect(currentStreamer.requireStreamerId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public YoutubeStatus status() {
        return pollingService.status(currentStreamer.requireStreamerId());
    }
}
