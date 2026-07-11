package com.streamerui.controller;

import com.streamerui.model.Badge;
import com.streamerui.partnership.PartnerInfo;
import com.streamerui.partnership.PartnerVisibilityService;
import com.streamerui.repository.BadgeLibraryRepository;
import com.streamerui.security.CurrentStreamerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CRUD for the current streamer's badge library (see BadgeLibraryRepository).
 * Badges created here show up as options in the "add badge" dropdown when
 * editing a user, so an image only has to be uploaded once and can be reused
 * across as many chatters as you like.
 *
 * GET also includes badges owned by any accepted partner streamer (see
 * PartnerVisibilityService) so partnered streamers can use each other's
 * badges - those come back with mine=false and ownerStreamerId/
 * ownerDisplayName set; only badges you own can be edited/deleted here.
 */
@RestController
@RequestMapping("/api/badges")
public class BadgeLibraryController {

    private final BadgeLibraryRepository repository;
    private final CurrentStreamerService currentStreamer;
    private final PartnerVisibilityService partnerVisibility;

    public BadgeLibraryController(BadgeLibraryRepository repository,
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
            for (Badge badge : repository.findAll(partner.getStreamerId())) {
                badge.setMine(false);
                badge.setOwnerStreamerId(partner.getStreamerId());
                badge.setOwnerDisplayName(partner.getDisplayName());
                combined.add(badge);
            }
        }
        return combined;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Badge badge) {
        if (badge.getImageUrl() == null || badge.getImageUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "imageUrl is required"));
        }
        badge.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(repository.save(currentStreamer.requireStreamerId(), badge));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Badge> update(@PathVariable String id, @RequestBody Badge badge) {
        badge.setId(id);
        return ResponseEntity.ok(repository.save(currentStreamer.requireStreamerId(), badge));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(currentStreamer.requireStreamerId(), id);
        return ResponseEntity.noContent().build();
    }
}
