package com.streamerui.controller;

import com.streamerui.model.Badge;
import com.streamerui.partnership.PartnerInfo;
import com.streamerui.partnership.PartnerVisibilityService;
import com.streamerui.repository.BannerLibraryRepository;
import com.streamerui.security.CurrentStreamerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CRUD for the current streamer's banner library (see BannerLibraryRepository).
 * Mirrors BadgeLibraryController exactly, including partner visibility on
 * GET - see that class for details.
 */
@RestController
@RequestMapping("/api/banners")
public class BannerLibraryController {

    private final BannerLibraryRepository repository;
    private final CurrentStreamerService currentStreamer;
    private final PartnerVisibilityService partnerVisibility;

    public BannerLibraryController(BannerLibraryRepository repository,
                                    CurrentStreamerService currentStreamer,
                                    PartnerVisibilityService partnerVisibility) {
        this.repository = repository;
        this.currentStreamer = currentStreamer;
        this.partnerVisibility = partnerVisibility;
    }

    @GetMapping
    public List<Badge> findAll() {
        Long me = currentStreamer.requireStreamerId();
        List<Badge> combined = new ArrayList<>(repository.findAll(me));
        for (PartnerInfo partner : partnerVisibility.acceptedPartners(me)) {
            for (Badge banner : repository.findAll(partner.getStreamerId())) {
                banner.setMine(false);
                banner.setOwnerStreamerId(partner.getStreamerId());
                banner.setOwnerDisplayName(partner.getDisplayName());
                combined.add(banner);
            }
        }
        return combined;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Badge banner) {
        if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "imageUrl is required"));
        }
        banner.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(repository.save(currentStreamer.requireStreamerId(), banner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Badge> update(@PathVariable String id, @RequestBody Badge banner) {
        banner.setId(id);
        return ResponseEntity.ok(repository.save(currentStreamer.requireStreamerId(), banner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(currentStreamer.requireStreamerId(), id);
        return ResponseEntity.noContent().build();
    }
}
