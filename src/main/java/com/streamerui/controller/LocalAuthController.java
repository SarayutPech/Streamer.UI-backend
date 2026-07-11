package com.streamerui.controller;

import com.streamerui.repository.jpa.StreamerEntity;
import com.streamerui.repository.jpa.StreamerJpaRepository;
import com.streamerui.security.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Plain username/password registration and login for the multi-tenant
 * (mysql profile) build. This is the demo auth path used instead of OAuth2
 * login (see SecurityConfig javadoc) - it's simpler to stand up without
 * registering an app with Google/Facebook first.
 *
 * Both endpoints are listed under /api/auth/** which SecurityConfig permits
 * without a token; everything else under /api/** requires the JWT this
 * issues.
 */
@RestController
@RequestMapping("/api/auth")
@Profile("mysql")
public class LocalAuthController {

    private final StreamerJpaRepository streamerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LocalAuthController(StreamerJpaRepository streamerRepository,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.streamerRepository = streamerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public AuthTokenResponse register(@RequestBody RegisterRequest request) {
        String username = normalize(request.getUsername());
        String password = request.getPassword();
        String email = request.getEmail() == null ? null : request.getEmail().trim();

        if (username.isEmpty() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password are required");
        }
        if (password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password must be at least 6 characters");
        }
        if (streamerRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already taken");
        }

        StreamerEntity streamer = new StreamerEntity();
        streamer.setUsername(username);
        streamer.setDisplayName(username);
        streamer.setPrimaryEmail(email);
        streamer.setPasswordHash(passwordEncoder.encode(password));
        streamer = streamerRepository.save(streamer);

        String token = jwtService.generateToken(streamer.getId());
        return new AuthTokenResponse(token);
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@RequestBody LoginRequest request) {
        String username = normalize(request.getUsername());
        String password = request.getPassword();

        StreamerEntity streamer = streamerRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid username or password"));

        if (streamer.getPasswordHash() == null
                || password == null
                || !passwordEncoder.matches(password, streamer.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid username or password");
        }

        String token = jwtService.generateToken(streamer.getId());
        return new AuthTokenResponse(token);
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim();
    }
}
