package com.streamerui.controller;

import com.streamerui.model.Badge;
import com.streamerui.repository.BannerLibraryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CRUD for the shared banner library (see BannerLibraryRepository). Banners
 * created here show up as options in the "banner" dropdown when editing a
 * user, so an image only has to be uploaded once and can be reused across
 * as many chatters as you like - same idea as the badge library.
 */
@RestController
@RequestMapping("/api/banners")
public class BannerLibraryController {

    private final BannerLibraryRepository repository;

    public BannerLibraryController(BannerLibraryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Badge> findAll() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Badge banner) {
        if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "imageUrl is required"));
        }
        banner.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(repository.save(banner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Badge> update(@PathVariable String id, @RequestBody Badge banner) {
        banner.setId(id);
        return ResponseEntity.ok(repository.save(banner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
