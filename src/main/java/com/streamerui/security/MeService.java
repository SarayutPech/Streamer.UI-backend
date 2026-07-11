package com.streamerui.security;

/** Backs GET /api/me - see FixedMeService (local) and JpaMeService (mysql/production). */
public interface MeService {
    MeResponse getMe(Long streamerId);
}
