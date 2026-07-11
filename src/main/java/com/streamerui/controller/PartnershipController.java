package com.streamerui.controller;

import com.streamerui.partnership.PartnershipDto;
import com.streamerui.partnership.StreamerSummaryDto;
import com.streamerui.repository.jpa.PartnershipEntity;
import com.streamerui.repository.jpa.PartnershipJpaRepository;
import com.streamerui.repository.jpa.StreamerEntity;
import com.streamerui.repository.jpa.StreamerJpaRepository;
import com.streamerui.security.CurrentStreamerService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Streamer-to-streamer partnership requests. Multi-tenant/production only
 * (@Profile("mysql")) - local/single-tenant dev mode has just the one
 * implicit streamer, so there's nothing to partner with.
 */
@RestController
@RequestMapping("/api")
@Profile("mysql")
public class PartnershipController {

    private final PartnershipJpaRepository partnerships;
    private final StreamerJpaRepository streamers;
    private final CurrentStreamerService currentStreamer;

    public PartnershipController(PartnershipJpaRepository partnerships, StreamerJpaRepository streamers,
                                  CurrentStreamerService currentStreamer) {
        this.partnerships = partnerships;
        this.streamers = streamers;
        this.currentStreamer = currentStreamer;
    }

    @GetMapping("/streamers/search")
    public List<StreamerSummaryDto> search(@RequestParam("q") String query) {
        Long me = currentStreamer.requireStreamerId();
        return streamers.findByDisplayNameContainingIgnoreCase(query).stream()
                .filter(s -> !s.getId().equals(me))
                .map(s -> new StreamerSummaryDto(s.getId(), s.getDisplayName(), s.getAvatarUrl()))
                .collect(Collectors.toList());
    }

    @GetMapping("/partnerships")
    public List<PartnershipDto> list() {
        Long me = currentStreamer.requireStreamerId();
        return partnerships.findAllInvolving(me).stream()
                .map(p -> toDto(p, me))
                .collect(Collectors.toList());
    }

    @PostMapping("/partnerships/request")
    public ResponseEntity<?> request(@RequestBody Map<String, Long> body) {
        Long me = currentStreamer.requireStreamerId();
        Long addresseeId = body.get("addresseeStreamerId");
        if (addresseeId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "addresseeStreamerId is required"));
        }
        if (addresseeId.equals(me)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot partner with yourself"));
        }
        if (streamers.findById(addresseeId).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No such streamer"));
        }
        if (partnerships.findActiveBetween(me, addresseeId).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "A pending or accepted partnership already exists with this streamer"));
        }
        PartnershipEntity p = new PartnershipEntity();
        p.setRequesterStreamerId(me);
        p.setAddresseeStreamerId(addresseeId);
        p.setStatus(PartnershipEntity.Status.pending);
        p = partnerships.save(p);
        return ResponseEntity.ok(toDto(p, me));
    }

    @PostMapping("/partnerships/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        return respond(id, PartnershipEntity.Status.accepted, true);
    }

    @PostMapping("/partnerships/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        return respond(id, PartnershipEntity.Status.rejected, true);
    }

    /** Either side can revoke an accepted (or their own pending outgoing) partnership. */
    @DeleteMapping("/partnerships/{id}")
    public ResponseEntity<?> revoke(@PathVariable Long id) {
        Long me = currentStreamer.requireStreamerId();
        PartnershipEntity p = partnerships.findById(id).orElse(null);
        if (p == null || !p.involves(me)) {
            return ResponseEntity.notFound().build();
        }
        p.setStatus(PartnershipEntity.Status.revoked);
        p.setRespondedAt(LocalDateTime.now());
        partnerships.save(p);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> respond(Long id, PartnershipEntity.Status newStatus, boolean addresseeOnly) {
        Long me = currentStreamer.requireStreamerId();
        PartnershipEntity p = partnerships.findById(id).orElse(null);
        if (p == null || !p.involves(me)) {
            return ResponseEntity.notFound().build();
        }
        if (addresseeOnly && !p.getAddresseeStreamerId().equals(me)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only the addressee can respond to this request"));
        }
        if (p.getStatus() != PartnershipEntity.Status.pending) {
            return ResponseEntity.status(409).body(Map.of("error", "This request is no longer pending"));
        }
        p.setStatus(newStatus);
        p.setRespondedAt(LocalDateTime.now());
        partnerships.save(p);
        return ResponseEntity.ok(toDto(p, me));
    }

    private PartnershipDto toDto(PartnershipEntity p, Long me) {
        Long otherId = p.otherStreamerId(me);
        String otherName = streamers.findById(otherId).map(StreamerEntity::getDisplayName).orElse("Unknown streamer");
        String direction = p.getRequesterStreamerId().equals(me) ? "outgoing" : "incoming";
        return new PartnershipDto(p.getId(), otherId, otherName, direction, p.getStatus().name());
    }
}
