package com.streamerui.model;

public class YoutubeStatus {

    private boolean connected;
    private String videoId;
    private String liveChatId;
    private String lastError;
    private long lastPolledAtEpochMs;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getLiveChatId() {
        return liveChatId;
    }

    public void setLiveChatId(String liveChatId) {
        this.liveChatId = liveChatId;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public long getLastPolledAtEpochMs() {
        return lastPolledAtEpochMs;
    }

    public void setLastPolledAtEpochMs(long lastPolledAtEpochMs) {
        this.lastPolledAtEpochMs = lastPolledAtEpochMs;
    }
}
