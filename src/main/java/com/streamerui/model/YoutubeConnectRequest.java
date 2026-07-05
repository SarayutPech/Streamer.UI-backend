package com.streamerui.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for POST /api/youtube/connect. Accepts either a bare video id or a
 * full YouTube URL (watch/live/shorts) in videoUrlOrId - the service
 * extracts the id. No API key needed - see YoutubeApiClient for how chat is
 * read without one.
 */
public class YoutubeConnectRequest {

    @NotBlank
    private String videoUrlOrId;

    public String getVideoUrlOrId() {
        return videoUrlOrId;
    }

    public void setVideoUrlOrId(String videoUrlOrId) {
        this.videoUrlOrId = videoUrlOrId;
    }
}
