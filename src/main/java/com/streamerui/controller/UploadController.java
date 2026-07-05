package com.streamerui.controller;

import com.streamerui.config.StreamerUiProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles badge/banner image uploads from the admin UI. Files are saved into
 * the uploads directory (served statically at /uploads/** - see WebConfig)
 * and the returned relative URL is what gets stored on a UserProfile.
 */
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "image/svg+xml");

    private final StreamerUiProperties properties;

    public UploadController(StreamerUiProperties properties) {
        this.properties = properties;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unsupported file type: " + contentType));
        }

        try {
            // IMPORTANT: this must be an absolute path. MultipartFile.transferTo()
            // is backed by jakarta.servlet.http.Part.write(), which resolves a
            // *relative* destination against the servlet container's own internal
            // work directory (e.g. .../Tomcat/localhost/ROOT/./uploads/...) rather
            // than the app's working directory - causing a confusing
            // FileNotFoundException even though "./uploads" exists right next to
            // the running app. Spring only falls back to a plain stream copy (which
            // uses the path as-is) when the destination File.isAbsolute() is true.
            File dir = new File(properties.getUploadsDir()).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String extension = extensionFor(contentType);
            String filename = UUID.randomUUID() + extension;
            File dest = new File(dir, filename);
            file.transferTo(dest);
            return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save file: " + e.getMessage()));
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> "";
        };
    }
}
