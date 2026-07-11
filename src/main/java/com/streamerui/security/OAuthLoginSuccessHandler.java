package com.streamerui.security;

import com.streamerui.config.StreamerUiProperties;
import com.streamerui.repository.jpa.OAuthIdentityEntity;
import com.streamerui.repository.jpa.OAuthIdentityJpaRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

/**
 * Runs after Spring Security's OAuth2 login flow finishes (Google/Facebook/
 * YouTube - see application-prod.yml for the 3 registrations). Creates or
 * updates the Streamer + OAuthIdentity rows, mints a JWT, and redirects the
 * browser back to the frontend with it in the URL fragment - the frontend's
 * /auth/callback route reads it from there (see JwtService/JwtAuthFilter for
 * how it's used afterward).
 */
@Component
@Profile("mysql")
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuthLoginSuccessHandler.class);

    private final StreamerAccountService accountService;
    private final OAuthIdentityJpaRepository identities;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final String frontendUrl;

    public OAuthLoginSuccessHandler(StreamerAccountService accountService,
                                     OAuthIdentityJpaRepository identities,
                                     OAuth2AuthorizedClientService authorizedClientService,
                                     JwtService jwtService,
                                     RestTemplate restTemplate,
                                     StreamerUiProperties properties) {
        this.accountService = accountService;
        this.identities = identities;
        this.authorizedClientService = authorizedClientService;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.frontendUrl = properties.getFrontendUrl();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String registrationId = token.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = token.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        OAuthIdentityEntity.Provider provider = OAuthIdentityEntity.Provider.valueOf(registrationId);
        ProfileFields fields = extractProfileFields(provider, attrs);

        StreamerAccountService.LoginResult result = accountService.findOrCreateForLogin(
                provider, fields.providerUserId, fields.email, fields.displayName, fields.avatarUrl);

        if (provider == OAuthIdentityEntity.Provider.youtube) {
            fetchAndStoreYoutubeChannelId(registrationId, token.getName(), result.identity);
        }

        String jwt = jwtService.generateToken(result.streamerId);
        String redirectUrl = frontendUrl + "/auth/callback#token=" + jwt;
        response.sendRedirect(redirectUrl);
    }

    private void fetchAndStoreYoutubeChannelId(String registrationId, String principalName, OAuthIdentityEntity identity) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(registrationId, principalName);
            if (client == null || client.getAccessToken() == null) {
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(client.getAccessToken().getTokenValue());
            var response = restTemplate.exchange(
                    "https://www.googleapis.com/youtube/v3/channels?part=id&mine=true",
                    HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<?, ?> body = response.getBody();
            if (body != null && body.get("items") instanceof java.util.List<?> items && !items.isEmpty()
                    && items.get(0) instanceof Map<?, ?> firstItem) {
                Object channelId = firstItem.get("id");
                if (channelId != null) {
                    identity.setYoutubeChannelId(channelId.toString());
                    identities.save(identity);
                }
            }
        } catch (Exception e) {
            // Non-fatal: login still succeeds, they just won't have
            // youtubeChannelId pre-filled and can paste their channel/video
            // URL manually when connecting chat.
            log.warn("Could not resolve YouTube channel id for this login: {}", e.getMessage());
        }
    }

    private ProfileFields extractProfileFields(OAuthIdentityEntity.Provider provider, Map<String, Object> attrs) {
        return switch (provider) {
            case google, youtube -> new ProfileFields(
                    str(attrs.get("sub")), str(attrs.get("email")), str(attrs.get("name")), str(attrs.get("picture")));
            case facebook -> new ProfileFields(
                    str(attrs.get("id")), str(attrs.get("email")), str(attrs.get("name")), facebookPictureUrl(attrs));
        };
    }

    @SuppressWarnings("unchecked")
    private String facebookPictureUrl(Map<String, Object> attrs) {
        try {
            Object picture = attrs.get("picture");
            if (picture instanceof Map<?, ?> pictureMap) {
                Object data = pictureMap.get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    Object url = dataMap.get("url");
                    return url != null ? url.toString() : null;
                }
            }
        } catch (Exception ignored) {
            // fall through to null
        }
        return null;
    }

    private String str(Object o) {
        return o != null ? o.toString() : null;
    }

    private record ProfileFields(String providerUserId, String email, String displayName, String avatarUrl) {
    }
}
