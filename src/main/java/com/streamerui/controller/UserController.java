package com.streamerui.controller;

import com.streamerui.model.UserProfile;
import com.streamerui.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CRUD for per-user chat appearance (badges, name color/glow, banner).
 * Backed by UserProfileRepository, currently the JSON-file implementation.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileRepository repository;

    public UserController(UserProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<UserProfile> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<UserProfile> findOne(@PathVariable String channelId) {
        return repository.findByChannelId(channelId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserProfile profile) {
        if (profile.getChannelId() == null || profile.getChannelId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "channelId is required"));
        }
        if (repository.findByChannelId(profile.getChannelId()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "A profile for this channelId already exists"));
        }
        return ResponseEntity.ok(repository.save(profile));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<UserProfile> update(@PathVariable String channelId, @RequestBody UserProfile profile) {
        profile.setChannelId(channelId);
        return ResponseEntity.ok(repository.save(profile));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(@PathVariable String channelId) {
        repository.deleteByChannelId(channelId);
        return ResponseEntity.noContent().build();
    }

    /** Wipes every saved user profile. Used by the "Clear all users" button in the admin UI. */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
