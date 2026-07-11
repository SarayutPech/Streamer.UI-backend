package com.streamerui.security;

import java.util.List;

/** Response body for GET /api/me. */
public class MeResponse {
    private Long streamerId;
    private String displayName;
    private String avatarUrl;
    private List<String> linkedProviders;

    public MeResponse() {
    }

    public MeResponse(Long streamerId, String displayName, String avatarUrl, List<String> linkedProviders) {
        this.streamerId = streamerId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.linkedProviders = linkedProviders;
    }

    public Long getStreamerId() {
        return streamerId;
    }

    public void setStreamerId(Long streamerId) {
        this.streamerId = streamerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getLinkedProviders() {
        return linkedProviders;
    }

    public void setLinkedProviders(List<String> linkedProviders) {
        this.linkedProviders = linkedProviders;
    }
}
