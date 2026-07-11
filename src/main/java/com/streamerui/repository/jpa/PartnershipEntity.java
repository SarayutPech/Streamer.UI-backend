package com.streamerui.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Request/accept relationship between two streamers. The DB also maintains
 * a generated "active_pair_key" column (not mapped here - Hibernate doesn't
 * need to know about it) that enforces at most one pending/accepted row per
 * pair of streamers at a time.
 */
@Entity
@Table(name = "partnerships")
public class PartnershipEntity {

    public enum Status { pending, accepted, rejected, revoked }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_streamer_id", nullable = false)
    private Long requesterStreamerId;

    @Column(name = "addressee_streamer_id", nullable = false)
    private Long addresseeStreamerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.pending;

    @Column(name = "requested_at", insertable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterStreamerId() {
        return requesterStreamerId;
    }

    public void setRequesterStreamerId(Long requesterStreamerId) {
        this.requesterStreamerId = requesterStreamerId;
    }

    public Long getAddresseeStreamerId() {
        return addresseeStreamerId;
    }

    public void setAddresseeStreamerId(Long addresseeStreamerId) {
        this.addresseeStreamerId = addresseeStreamerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    /** True if this row involves the given streamer as either side. */
    public boolean involves(Long streamerId) {
        return requesterStreamerId.equals(streamerId) || addresseeStreamerId.equals(streamerId);
    }

    /** The *other* streamer in this partnership, relative to streamerId. */
    public Long otherStreamerId(Long streamerId) {
        return requesterStreamerId.equals(streamerId) ? addresseeStreamerId : requesterStreamerId;
    }
}
