package com.streamerui.controller;

import com.streamerui.model.UserProfile;
import com.streamerui.repository.UserProfileRepository;
import com.streamerui.security.CurrentStreamerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CRUD for the current streamer's per-user chat appearance (badges, name
 * color/glow, banner). Backed by UserProfileRepository, scoped to whoever is
 * currently authenticated (see CurrentStreamerService).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileRepository repository;
    private final CurrentStreamerService currentStreamer;

    public UserController(UserProfileRepository repository, CurrentStreamerService currentStreamer) {
        this.repository = repository;
        this.currentStreamer = currentStreamer;
    }

    @GetMapping
    public List<UserProfile> findAll() {
        return repository.findAll(currentStreamer.requireStreamerId());
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<UserProfile> findOne(@PathVariable String channelId) {
        return repository.findByChannelId(currentStreamer.requireStreamerId(), channelId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserProfile profile) {
        if (profile.getChannelId() == null || profile.getChannelId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "channelId is required"));
        }
        Long streamerId = currentStreamer.requireStreamerId();
        if (repository.findByChannelId(streamerId, profile.getChannelId()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "A profile for this channelId already exists"));
        }
        return ResponseEntity.ok(repository.save(streamerId, profile));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<UserProfile> update(@PathVariable String channelId, @RequestBody UserProfile profile) {
        profile.setChannelId(channelId);
        return ResponseEntity.ok(repository.save(currentStreamer.requireStreamerId(), profile));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(@PathVariable String channelId) {
        repository.deleteByChannelId(currentStreamer.requireStreamerId(), channelId);
        return ResponseEntity.noContent().build();
    }

    /** Wipes every saved user profile for this streamer. Used by the "Clear all users" button in the admin UI. */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        repository.deleteAll(currentStreamer.requireStreamerId());
        return ResponseEntity.noContent().build();
    }
}
