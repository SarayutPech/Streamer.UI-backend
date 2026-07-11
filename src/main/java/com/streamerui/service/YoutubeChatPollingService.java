package com.streamerui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamerui.config.StreamerUiProperties;
import com.streamerui.model.Badge;
import com.streamerui.model.ChatMessageDto;
import com.streamerui.model.OverlayConfig;
import com.streamerui.model.UserProfile;
import com.streamerui.model.YoutubeStatus;
import com.streamerui.repository.OverlayConfigRepository;
import com.streamerui.repository.UserProfileRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Owns one YouTube live chat connection per streamer (keyed by streamerId -
 * each streamer can have at most one active connection at a time, but
 * multiple streamers can be connected simultaneously). No API key needed:
 * this reads the same internal endpoint the live chat popout page itself
 * uses (see YoutubeApiClient for details/caveats). Each new message is
 * enriched with the author's saved UserProfile (badges/name color/glow/
 * banner) for that streamer and broadcast to that streamer's overlay over
 * WebSocket.
 *
 * Also doubles as the home for "send a preview message" (see
 * sendPreviewMessage) so the admin can test badge/glow/banner/event styling
 * without waiting on a real live chat - it goes through the exact same
 * enrichment logic as a real message (buildDto).
 */
@Service
public class YoutubeChatPollingService {

    private static final Logger log = LoggerFactory.getLogger(YoutubeChatPollingService.class);

    private final YoutubeApiClient client;
    private final UserProfileRepository userProfileRepository;
    private final OverlayConfigRepository overlayConfigRepository;
    private final ChatBroadcastService broadcastService;
    private final long minPollIntervalMs;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "youtube-chat-poller");
        t.setDaemon(true);
        return t;
    });

    /** One poll state per streamer that has connected at least once. */
    private final Map<Long, PollState> statesByStreamer = new ConcurrentHashMap<>();

    public YoutubeChatPollingService(YoutubeApiClient client,
                                      UserProfileRepository userProfileRepository,
                                      OverlayConfigRepository overlayConfigRepository,
                                      ChatBroadcastService broadcastService,
                                      StreamerUiProperties properties) {
        this.client = client;
        this.userProfileRepository = userProfileRepository;
        this.overlayConfigRepository = overlayConfigRepository;
        this.broadcastService = broadcastService;
        this.minPollIntervalMs = properties.getYoutube().getMinPollIntervalMs();
    }

    public synchronized YoutubeStatus connect(Long streamerId, String videoUrlOrId) {
        stopInternal(streamerId);

        String videoId = VideoIdExtractor.extract(videoUrlOrId);
        YoutubeApiClient.Session session = client.openSession(videoId);

        PollState newState = new PollState();
        newState.videoId = videoId;
        newState.session = session;
        newState.connected = true;
        statesByStreamer.put(streamerId, newState);

        schedulePoll(streamerId, 0);
        log.info("Connected to YouTube live chat (unofficial popout endpoint). streamerId={} videoId={}", streamerId, videoId);
        return status(streamerId);
    }

    public synchronized void disconnect(Long streamerId) {
        stopInternal(streamerId);
        log.info("Disconnected from YouTube live chat. streamerId={}", streamerId);
    }

    private void stopInternal(Long streamerId) {
        PollState existing = statesByStreamer.get(streamerId);
        if (existing != null && existing.scheduledFuture != null) {
            existing.scheduledFuture.cancel(true);
        }
        statesByStreamer.put(streamerId, new PollState());
    }

    public synchronized YoutubeStatus status(Long streamerId) {
        PollState s = statesByStreamer.getOrDefault(streamerId, new PollState());
        YoutubeStatus dto = new YoutubeStatus();
        dto.setConnected(s.connected);
        dto.setVideoId(s.videoId);
        dto.setLiveChatId(s.session != null ? s.session.continuation : null);
        dto.setLastError(s.lastError);
        dto.setLastPolledAtEpochMs(s.lastPolledAtEpochMs);
        return dto;
    }

    /**
     * Builds a synthetic chat message through the same enrichment pipeline as
     * a real one (profile lookup, badge capping, name color/glow/banner) and
     * broadcasts it immediately to this streamer's overlay. Used by the
     * admin's "send preview message" feature - no live YouTube connection
     * required.
     */
    public ChatMessageDto sendPreviewMessage(Long streamerId, String channelId, String displayName, String type,
                                              String messageText, String amountDisplayString) {
        String id = "preview-" + UUID.randomUUID();
        String safeType = (type == null || type.isBlank()) ? "textMessageEvent" : type;
        ChatMessageDto dto = buildDto(streamerId, id, safeType, null, channelId, displayName, null, messageText, amountDisplayString);
        broadcastService.broadcastMessage(streamerId, dto);
        return dto;
    }

    private void schedulePoll(Long streamerId, long delayMs) {
        PollState s = statesByStreamer.get(streamerId);
        if (s == null) {
            return;
        }
        s.scheduledFuture = executor.schedule(() -> pollOnce(streamerId), delayMs, TimeUnit.MILLISECONDS);
    }

    private void pollOnce(Long streamerId) {
        PollState s = statesByStreamer.get(streamerId);
        if (s == null || !s.connected) {
            return;
        }
        long nextDelay = Math.max(minPollIntervalMs, 4000);
        try {
            YoutubeApiClient.PollResult result = client.fetchNext(s.session);
            for (JsonNode item : result.items) {
                try {
                    ChatMessageDto dto = mapToDto(streamerId, item);
                    if (dto != null) {
                        broadcastService.broadcastMessage(streamerId, dto);
                    }
                } catch (Exception itemError) {
                    log.warn("Skipping malformed chat item: {}", itemError.getMessage());
                }
            }
            s.session = result.nextSession;
            s.lastError = null;
            s.lastPolledAtEpochMs = System.currentTimeMillis();
            nextDelay = Math.max(result.pollingIntervalMillis, minPollIntervalMs);
        } catch (Exception e) {
            log.warn("YouTube poll failed for streamerId={}: {}", streamerId, e.getMessage());
            s.lastError = e.getMessage();
        }

        if (s.connected) {
            schedulePoll(streamerId, nextDelay);
        }
    }

    private ChatMessageDto mapToDto(Long streamerId, JsonNode item) {
        // The renderer type name changes depending on the kind of chat item.
        JsonNode renderer = firstPresent(item,
                "liveChatTextMessageRenderer",
                "liveChatPaidMessageRenderer",
                "liveChatPaidStickerRenderer",
                "liveChatMembershipItemRenderer");
        if (renderer == null) {
            return null; // system/engagement messages etc. - nothing to show
        }

        String channelId = renderer.path("authorExternalChannelId").asText(null);
        if (channelId == null || channelId.isBlank()) {
            return null;
        }
        String displayName = renderer.at("/authorName/simpleText").asText("Unknown");
        String type = rendererTypeToEventType(item);
        String profileImageUrl = lastThumbnailUrl(renderer.at("/authorPhoto/thumbnails"));
        String messageText = extractMessageText(renderer, type);
        String amountDisplayString = renderer.at("/purchaseAmountText/simpleText").asText(null);
        String publishedAt = renderer.path("timestampUsec").asText(null);
        String id = renderer.path("id").asText(null);

        return buildDto(streamerId, id, type, publishedAt, channelId, displayName, profileImageUrl, messageText, amountDisplayString);
    }

    /**
     * Shared enrichment step: looks up (or creates) the author's saved
     * UserProfile and the current OverlayConfig for this streamer, then
     * merges everything into a ChatMessageDto ready to broadcast. Used for
     * both real YouTube messages and preview/test messages.
     */
    private ChatMessageDto buildDto(Long streamerId, String id, String type, String publishedAt, String channelId,
                                     String displayName, String profileImageUrl, String messageText,
                                     String amountDisplayString) {
        UserProfile profile = userProfileRepository.findOrCreate(streamerId, channelId, displayName);
        OverlayConfig config = overlayConfigRepository.get(streamerId);

        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(id);
        dto.setType(type);
        dto.setPublishedAt(publishedAt);
        dto.setChannelId(channelId);
        dto.setDisplayName(displayName);
        dto.setProfileImageUrl(profileImageUrl);
        dto.setMessageText(messageText);
        dto.setAmountDisplayString(amountDisplayString);

        int slotCount = Math.max(0, config.getBadgeSlotCount());
        List<Badge> cappedBadges = profile.getBadges() == null
                ? List.of()
                : profile.getBadges().stream().limit(slotCount).collect(Collectors.toList());
        dto.setBadges(cappedBadges);
        dto.setNameColor(profile.getNameColor() != null ? profile.getNameColor() : config.getDefaultNameColor());
        dto.setGlowEnabled(profile.isGlowEnabled());
        dto.setGlowColor(profile.getGlowColor());
        dto.setGlowSize(profile.getGlowSize());
        dto.setBannerImageUrl(profile.getBannerImageUrl());
        dto.setBannerEnabled(profile.isBannerEnabled());
        dto.setBannerMessage(profile.getBannerMessage());
        return dto;
    }

    private JsonNode firstPresent(JsonNode item, String... fieldNames) {
        for (String name : fieldNames) {
            JsonNode node = item.path(name);
            if (!node.isMissingNode() && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private String rendererTypeToEventType(JsonNode item) {
        if (!item.path("liveChatPaidMessageRenderer").isMissingNode()) return "superChatEvent";
        if (!item.path("liveChatPaidStickerRenderer").isMissingNode()) return "superStickerEvent";
        if (!item.path("liveChatMembershipItemRenderer").isMissingNode()) return "newSponsorEvent";
        return "textMessageEvent";
    }

    private String extractMessageText(JsonNode renderer, String type) {
        if ("superStickerEvent".equals(type)) {
            return "[Super Sticker]";
        }
        if ("newSponsorEvent".equals(type)) {
            String headerText = joinRuns(renderer.at("/headerSubtext/runs"));
            return headerText.isBlank() ? "started a new membership!" : headerText;
        }
        JsonNode runs = renderer.at("/message/runs");
        String text = joinRuns(runs);
        return text;
    }

    private String joinRuns(JsonNode runs) {
        if (!runs.isArray()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode run : runs) {
            if (run.has("text")) {
                sb.append(run.path("text").asText(""));
            } else if (run.has("emoji")) {
                JsonNode shortcuts = run.at("/emoji/shortcuts");
                if (shortcuts.isArray() && shortcuts.size() > 0) {
                    sb.append(shortcuts.get(0).asText(""));
                }
            }
        }
        return sb.toString();
    }

    private String lastThumbnailUrl(JsonNode thumbnails) {
        if (!thumbnails.isArray() || thumbnails.isEmpty()) {
            return null;
        }
        return thumbnails.get(thumbnails.size() - 1).path("url").asText(null);
    }

    @PreDestroy
    public void shutdown() {
        for (Long streamerId : statesByStreamer.keySet()) {
            stopInternal(streamerId);
        }
        executor.shutdownNow();
    }

    private static class PollState {
        volatile String videoId;
        volatile YoutubeApiClient.Session session;
        volatile boolean connected = false;
        volatile String lastError;
        volatile long lastPolledAtEpochMs;
        volatile ScheduledFuture<?> scheduledFuture;
    }
}
