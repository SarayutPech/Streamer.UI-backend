package com.streamerui.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls an 11-character YouTube video id out of any of the common URL shapes,
 * or passes the input through unchanged if it already looks like a bare id.
 */
public final class VideoIdExtractor {

    private static final Pattern BARE_ID = Pattern.compile("^[a-zA-Z0-9_-]{10,12}$");
    private static final Pattern[] URL_PATTERNS = new Pattern[] {
            Pattern.compile("(?:youtube\\.com/watch\\?v=|youtube\\.com/watch\\?.*&v=)([a-zA-Z0-9_-]{10,12})"),
            Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]{10,12})"),
            Pattern.compile("youtube\\.com/live/([a-zA-Z0-9_-]{10,12})"),
            Pattern.compile("youtube\\.com/shorts/([a-zA-Z0-9_-]{10,12})"),
            Pattern.compile("youtube\\.com/embed/([a-zA-Z0-9_-]{10,12})")
    };

    private VideoIdExtractor() {
    }

    public static String extract(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (BARE_ID.matcher(trimmed).matches()) {
            return trimmed;
        }
        for (Pattern p : URL_PATTERNS) {
            Matcher m = p.matcher(trimmed);
            if (m.find()) {
                return m.group(1);
            }
        }
        throw new IllegalArgumentException("Could not extract a YouTube video id from: " + input);
    }
}
