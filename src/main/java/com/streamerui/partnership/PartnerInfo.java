package com.streamerui.partnership;

/** One accepted partner streamer, as seen from another streamer's perspective. */
public class PartnerInfo {
    private final Long streamerId;
    private final String displayName;

    public PartnerInfo(Long streamerId, String displayName) {
        this.streamerId = streamerId;
        this.displayName = displayName;
    }

    public Long getStreamerId() {
        return streamerId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
