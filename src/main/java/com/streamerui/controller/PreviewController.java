package com.streamerui.controller;

import com.streamerui.model.ChatMessageDto;
import com.streamerui.model.PreviewRequest;
import com.streamerui.security.CurrentStreamerService;
import com.streamerui.service.YoutubeChatPollingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets the admin UI send a fake chat message straight to their own overlay,
 * for testing badge/glow/banner/event styling without waiting on a real live
 * chat. Goes through the same profile-enrichment code as real messages.
 */
@RestController
@RequestMapping("/api/preview")
public class PreviewController {

    private final YoutubeChatPollingService pollingService;
    private final CurrentStreamerService currentStreamer;

    public PreviewController(YoutubeChatPollingService pollingService, CurrentStreamerService currentStreamer) {
        this.pollingService = pollingService;
        this.currentStreamer = currentStreamer;
    }

    @PostMapping
    public ChatMessageDto send(@RequestBody PreviewRequest request) {
        String channelId = blankToNull(request.getChannelId());
        String displayName = blankToNull(request.getDisplayName());
        if (channelId == null) {
            channelId = "PREVIEW_" + (displayName != null ? displayName : "Tester");
        }
        if (displayName == null) {
            displayName = "PreviewUser";
        }
        String messageText = blankToNull(request.getMessageText());
        if (messageText == null) {
            messageText = defaultMessageFor(request.getType());
        }
        String amount = blankToNull(request.getAmountDisplayString());
        if (amount == null && ("superChatEvent".equals(request.getType()) || "superStickerEvent".equals(request.getType()))) {
            amount = "$5.00";
        }
        return pollingService.sendPreviewMessage(currentStreamer.requireStreamerId(), channelId, displayName,
                request.getType(), messageText, amount);
    }

    private String defaultMessageFor(String type) {
        if ("superChatEvent".equals(type)) {
            return "Thanks for the great stream!";
        }
        if ("superStickerEvent".equals(type)) {
            return "[Super Sticker]";
        }
        if ("newSponsorEvent".equals(type)) {
            return "started a new membership!";
        }
        return "This is a preview chat message.";
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
