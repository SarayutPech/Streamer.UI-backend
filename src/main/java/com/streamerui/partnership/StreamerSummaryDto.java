package com.streamerui.partnership;

/** Minimal public info about a streamer, used for search results / partner lists. */
public class StreamerSummaryDto {
    private Long streamerId;
    private String displayName;
    private String avatarUrl;

    public StreamerSummaryDto() {
    }

    public StreamerSummaryDto(Long streamerId, String displayName, String avatarUrl) {
        this.streamerId = streamerId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
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
}
