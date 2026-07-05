package com.streamerui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "streamerui")
public class StreamerUiProperties {

    private String dataDir = "./data";
    private String uploadsDir = "./uploads";
    private Cors cors = new Cors();
    private Youtube youtube = new Youtube();

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
}
