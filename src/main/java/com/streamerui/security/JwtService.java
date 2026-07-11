package com.streamerui.security;

import com.streamerui.config.StreamerUiProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Issues and verifies the HS256 JWT handed to the frontend after a
 * successful OAuth2 login (see OAuthLoginSuccessHandler) and checked on
 * every subsequent API request (see JwtAuthFilter). The token's only
 * meaningful claim is the streamer id ("sub").
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(StreamerUiProperties properties) {
        String secret = properties.getJwt().getSecret();
        // HS256 needs a key of at least 256 bits; pad/hash short secrets so a
        // short local-dev secret doesn't blow up at startup.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = java.util.Arrays.copyOf(keyBytes, 32);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMinutes = properties.getJwt().getExpirationMinutes();
    }

    public String generateToken(Long streamerId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(streamerId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Returns the streamer id encoded in the token, or empty if invalid/expired. */
    public java.util.Optional<Long> parseStreamerId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return java.util.Optional.of(Long.parseLong(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }
}
