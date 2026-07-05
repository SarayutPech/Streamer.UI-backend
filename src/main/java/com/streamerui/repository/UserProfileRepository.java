package com.streamerui.repository;

import com.streamerui.model.UserProfile;

import java.util.List;
import java.util.Optional;

/**
 * Storage-agnostic contract for per-user chat appearance settings.
 * Today this is backed by a JSON file (see json.JsonUserProfileRepository).
 * To move to a real database, implement this interface against your DB of
 * choice (e.g. JPA/Postgres) and swap the @Repository bean - no other
 * class in the app needs to change.
 */
public interface UserProfileRepository {

    List<UserProfile> findAll();

    Optional<UserProfile> findByChannelId(String channelId);

    UserProfile save(UserProfile profile);

    void deleteByChannelId(String channelId);

    /** Wipes every saved profile (e.g. an admin "clear all users" action). */
    void deleteAll();

    /**
     * Returns the existing profile for the channelId, or creates and persists
     * a fresh default one if none exists yet (used when a brand new chatter
     * shows up in the live chat feed).
     */
    UserProfile findOrCreate(String channelId, String displayName);
}
