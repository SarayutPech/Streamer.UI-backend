package com.streamerui.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Production/multi-tenant mode: the streamer id comes from the JWT the
 * JwtAuthFilter validated for this request (set as the Authentication
 * principal). See SecurityConfig for how requests get here authenticated.
 */
@Service
@Profile("mysql")
public class SecurityCurrentStreamerService implements CurrentStreamerService {

    @Override
    public Long requireStreamerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new IllegalStateException("No authenticated streamer for this request");
        }
        return (Long) auth.getPrincipal();
    }
}
