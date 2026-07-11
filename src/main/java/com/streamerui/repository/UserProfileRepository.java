package com.streamerui.repository;

import com.streamerui.model.UserProfile;

import java.util.List;
import java.util.Optional;

/**
 * Storage-agnostic contract for per-user chat appearance settings, scoped to
 * one streamer (owner) at a time. Backed by a JSON file locally (see
 * json.JsonUserProfileRepository - which ignores streamerId, single-tenant
 * local dev only) or by MySQL in production (see jpa.JpaUserProfileRepository).
 */
public interface UserProfileRepository {

    List<UserProfile> findAll(Long streamerId);

    Optional<UserProfile> findByChannelId(Long streamerId, String channelId);

    UserProfile save(Long streamerId, UserProfile profile);

    void deleteByChannelId(Long streamerId, String channelId);

    /** Wipes every saved profile for this streamer (e.g. an admin "clear all users" action). */
    void deleteAll(Long streamerId);

    /**
     * Returns the existing profile for the channelId under this streamer, or
     * creates and persists a fresh default one if none exists yet (used when
     * a brand new chatter shows up in the live chat feed).
     */
    UserProfile findOrCreate(Long streamerId, String channelId, String displayName);
}
