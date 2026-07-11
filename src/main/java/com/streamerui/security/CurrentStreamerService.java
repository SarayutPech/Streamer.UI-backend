package com.streamerui.security;

/**
 * Resolves "who is making this request" as a streamer id, so controllers
 * don't need to know whether the app is running in local/single-tenant mode
 * (FixedCurrentStreamerService) or real multi-tenant mode with OAuth login
 * (SecurityCurrentStreamerService).
 */
public interface CurrentStreamerService {

    /** The authenticated streamer's id. Throws if there isn't one. */
    Long requireStreamerId();
}
