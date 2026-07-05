package com.streamerui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads live chat WITHOUT the official YouTube Data API / API key.
 *
 * <p>Instead, this talks to the same undocumented "innertube" endpoint that
 * youtube.com's own JS uses to power the live chat popout page
 * ({@code https://www.youtube.com/live_chat?is_popout=1&v=VIDEO_ID}). Every
 * unofficial YouTube chat reader (pytchat, youtube-chat, chat-downloader,
 * etc.) works this same way: load the popout HTML once to grab a
 * continuation token + the page's own public web-client key, then repeatedly
 * POST that continuation to {@code youtubei/v1/live_chat/get_live_chat} to
 * get the next batch of messages plus the next continuation token.
 *
 * <p><b>This is unofficial and unsupported by Google.</b> It has no quota
 * limit and needs no API key, but it depends on the internal shape of
 * YouTube's own page, which can change without notice and isn't covered by
 * any compatibility guarantee. If it stops working, the fix is almost always
 * updating the JSON field paths below to match whatever YouTube changed.
 */
@Component
public class YoutubeApiClient {

    private static final String POPOUT_URL_TEMPLATE =
            "https://www.youtube.com/live_chat?is_popout=1&v=%s";
    private static final String INNERTUBE_URL_TEMPLATE =
            "https://www.youtube.com/youtubei/v1/live_chat/get_live_chat?key=%s";
    private static final String DEFAULT_CLIENT_VERSION = "2.20240401.01.00";

    private static final Pattern API_KEY_PATTERN = Pattern.compile("\"INNERTUBE_API_KEY\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CLIENT_VERSION_PATTERN =
            Pattern.compile("\"INNERTUBE_CONTEXT_CLIENT_VERSION\"\\s*:\\s*\"([^\"]+)\"");

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public YoutubeApiClient(RestTemplate restTemplate, ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /** Loads the popout chat page once and extracts everything needed to start polling. */
    public Session openSession(String videoId) {
        String html = getHtml(String.format(POPOUT_URL_TEMPLATE, videoId));

        String apiKey = firstGroup(API_KEY_PATTERN, html);
        if (apiKey == null) {
            throw new IllegalStateException(
                    "Couldn't find INNERTUBE_API_KEY on the popout page - YouTube may have changed its page format.");
        }
        String clientVersion = firstGroup(CLIENT_VERSION_PATTERN, html);
        if (clientVersion == null) {
            clientVersion = DEFAULT_CLIENT_VERSION;
        }

        String initialDataJson = JsonBlobExtractor.extractJsonObjectAfter(html, "ytInitialData");
        if (initialDataJson == null) {
            throw new IllegalStateException(
                    "Couldn't find ytInitialData on the popout page - the video id may be wrong, "
                            + "or YouTube changed its page format.");
        }

        JsonNode initialData = parseJson(initialDataJson);
        JsonNode liveChatRenderer = initialData.at("/contents/liveChatRenderer");
        if (liveChatRenderer.isMissingNode()) {
            throw new IllegalStateException(
                    "This video has no live chat right now (not live, chat disabled, or replay-only).");
        }

        String continuation = firstNonMissing(liveChatRenderer,
                "/continuations/0/invalidationContinuationData/continuation",
                "/continuations/0/timedContinuationData/continuation",
                "/continuations/0/reloadContinuationData/continuation",
                "/continuations/0/liveChatReplayContinuationData/continuation");
        if (continuation == null) {
            throw new IllegalStateException(
                    "Found the live chat but no continuation token - YouTube may have changed its page format.");
        }

        Session session = new Session();
        session.apiKey = apiKey;
        session.clientVersion = clientVersion;
        session.continuation = continuation;
        return session;
    }

    /** Fetches the next batch of chat actions and the continuation token to use after this one. */
    public PollResult fetchNext(Session session) {
        String url = String.format(INNERTUBE_URL_TEMPLATE, session.apiKey);

        String body = String.format(
                "{\"context\":{\"client\":{\"clientName\":\"WEB\",\"clientVersion\":\"%s\"}},\"continuation\":\"%s\"}",
                session.clientVersion, session.continuation);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", browserUserAgent());

        JsonNode root;
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            root = parseJson(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reach YouTube's live chat endpoint: " + e.getMessage(), e);
        }

        JsonNode liveChatContinuation = root.at("/continuationContents/liveChatContinuation");
        if (liveChatContinuation.isMissingNode()) {
            throw new IllegalStateException(
                    "Unexpected response shape from YouTube (no liveChatContinuation) - "
                            + "the stream may have ended, or YouTube changed its internal API.");
        }

        PollResult result = new PollResult();
        result.items = new ArrayList<>();
        for (JsonNode action : liveChatContinuation.path("actions")) {
            JsonNode item = action.at("/addChatItemAction/item");
            if (!item.isMissingNode()) {
                result.items.add(item);
            }
        }

        String nextContinuation = firstNonMissing(liveChatContinuation,
                "/continuations/0/invalidationContinuationData/continuation",
                "/continuations/0/timedContinuationData/continuation",
                "/continuations/0/reloadContinuationData/continuation");
        long timeoutMs = firstNonMissingLong(liveChatContinuation,
                "/continuations/0/invalidationContinuationData/timeoutMs",
                "/continuations/0/timedContinuationData/timeoutMs");

        Session nextSession = new Session();
        nextSession.apiKey = session.apiKey;
        nextSession.clientVersion = session.clientVersion;
        nextSession.continuation = nextContinuation != null ? nextContinuation : session.continuation;
        result.nextSession = nextSession;
        result.pollingIntervalMillis = timeoutMs > 0 ? timeoutMs : 5000;
        return result;
    }

    private String getHtml(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", browserUserAgent());
        headers.set("Accept-Language", "en-US,en;q=0.9");
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            String body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("Empty response from YouTube.");
            }
            return body;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load YouTube popout chat page: " + e.getMessage(), e);
        }
    }

    private JsonNode parseJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JSON from YouTube: " + e.getMessage(), e);
        }
    }

    private static String firstGroup(Pattern pattern, String input) {
        Matcher m = pattern.matcher(input);
        return m.find() ? m.group(1) : null;
    }

    private static String firstNonMissing(JsonNode base, String... pointers) {
        for (String pointer : pointers) {
            JsonNode node = base.at(pointer);
            if (!node.isMissingNode() && !node.isNull()) {
                return node.asText();
            }
        }
        return null;
    }

    private static long firstNonMissingLong(JsonNode base, String... pointers) {
        for (String pointer : pointers) {
            JsonNode node = base.at(pointer);
            if (!node.isMissingNode() && !node.isNull()) {
                return node.asLong();
            }
        }
        return 0;
    }

    private static String browserUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    }

    /** Everything needed to keep polling: which video, which innertube key/version, current continuation. */
    public static class Session {
        public String apiKey;
        public String clientVersion;
        public String continuation;
    }

    public static class PollResult {
        public List<JsonNode> items;
        public Session nextSession;
        public long pollingIntervalMillis;
    }
}
