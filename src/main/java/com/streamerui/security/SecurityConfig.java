package com.streamerui.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Two entirely separate security setups, picked by Spring profile:
 *
 * - "!mysql" (local/single-tenant dev, the default): no login, everything
 *   permitted - matches the app's original "just runs on your machine"
 *   behavior.
 * - "mysql" (production/multi-tenant): OAuth2 login (Google/Facebook/
 *   YouTube - see application-prod.yml) issues a JWT (OAuthLoginSuccessHandler),
 *   which JwtAuthFilter then validates on every subsequent API call. Public
 *   GET endpoints for the overlay (unauthenticated OBS browser source) stay
 *   open; everything else under /api/** requires a valid token.
 */
@Configuration
public class SecurityConfig {

    @Bean
    @Profile("!mysql")
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Profile("mysql")
    public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http,
                                                               JwtAuthFilter jwtAuthFilter,
                                                               OAuthLoginSuccessHandler successHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {}) // picks up WebConfig's /api/** and /uploads/** CORS mappings
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2.successHandler(successHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
