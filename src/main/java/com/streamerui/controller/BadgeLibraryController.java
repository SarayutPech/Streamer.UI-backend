package com.streamerui.controller;

import com.streamerui.model.Badge;
import com.streamerui.repository.BadgeLibraryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CRUD for the shared badge library (see BadgeLibraryRepository). Badges
 * created here show up as options in the "add badge" dropdown when editing
 * a user, so an image only has to be uploaded once and can be reused across
 * as many chatters as you like.
 */
@RestController
@RequestMapping("/api/badges")
public class BadgeLibraryController {

    private final BadgeLibraryRepository repository;

    public BadgeLibraryController(BadgeLibraryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Badge> findAll() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Badge badge) {
        if (badge.getImageUrl() == null || badge.getImageUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "imageUrl is required"));
        }
        badge.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(repository.save(badge));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Badge> update(@PathVariable String id, @RequestBody Badge badge) {
        badge.setId(id);
        return ResponseEntity.ok(repository.save(badge));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
