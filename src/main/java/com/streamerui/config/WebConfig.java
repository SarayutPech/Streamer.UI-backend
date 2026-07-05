package com.streamerui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Serves uploaded badge/banner images from the uploads directory at /uploads/**
 * and configures CORS for the REST API so the Vite dev server (and OBS browser
 * source, which sends no meaningful Origin) can reach the backend.
 *
 * Uploaded files get a fresh random UUID filename per upload and are never
 * overwritten in place (see UploadController) - the same URL always points at
 * the same bytes forever. That makes it safe to mark them cacheable for a
 * full year with "immutable", so the browser only ever downloads each badge/
 * banner/avatar-override image once instead of re-fetching it from the
 * backend on every overlay reload, admin page load, or new chat message that
 * reuses the same badge.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StreamerUiProperties properties;

    @Autowired
    public WebConfig(StreamerUiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + new File(properties.getUploadsDir()).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = properties.getCors().getAllowedOrigins().toArray(new String[0]);
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
        registry.addMapping("/uploads/**")
                .allowedOrigins("*")
                .allowedMethods("GET");
    }
}
