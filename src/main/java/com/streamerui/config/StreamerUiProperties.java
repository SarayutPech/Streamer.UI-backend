package com.streamerui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "streamerui")
public class StreamerUiProperties {

    private String dataDir = "./data";
    private String uploadsDir = "./uploads";
    private String frontendUrl = "http://localhost:5173";
    private Cors cors = new Cors();
    private Youtube youtube = new Youtube();
    private Jwt jwt = new Jwt();

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getUploadsDir() {
        return uploadsDir;
    }

    public void setUploadsDir(String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    /** Where to redirect the browser back to (with the JWT) after OAuth login succeeds. */
    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Youtube getYoutube() {
        return youtube;
    }

    public void setYoutube(Youtube youtube) {
        this.youtube = youtube;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("*");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Youtube {
        private long minPollIntervalMs = 2000L;

        public long getMinPollIntervalMs() {
            return minPollIntervalMs;
        }

        public void setMinPollIntervalMs(long minPollIntervalMs) {
            this.minPollIntervalMs = minPollIntervalMs;
        }
    }

    public static class Jwt {
        /** HS256 signing secret. Must be set (and kept secret) in production. */
        private String secret = "local-dev-only-secret-do-not-use-in-production-1234567890";
        private long expirationMinutes = 43200; // 30 days

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }
}
