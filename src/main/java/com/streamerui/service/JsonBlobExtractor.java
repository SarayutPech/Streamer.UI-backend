package com.streamerui.service;

/**
 * Pulls a single JSON object literal out of an HTML page, e.g. the
 * {@code var ytInitialData = {...};} blob YouTube embeds in its pages.
 * A naive regex like {@code \{.*\}} breaks on nested braces and braces that
 * appear inside string values, so this does a real balanced-brace scan that
 * understands JS string literals (quotes + backslash escapes).
 */
public final class JsonBlobExtractor {

    private JsonBlobExtractor() {
    }

    /**
     * Finds the first occurrence of {@code marker} in {@code html}, then the
     * first {@code '{'} after it, then returns the substring up to the
     * matching closing {@code '}'}. Returns null if the marker or a balanced
     * object can't be found.
     */
    public static String extractJsonObjectAfter(String html, String marker) {
        int markerIndex = html.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }
        int start = html.indexOf('{', markerIndex + marker.length());
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < html.length(); i++) {
            char c = html.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return html.substring(start, i + 1);
                }
            }
        }
        return null;
    }
}
