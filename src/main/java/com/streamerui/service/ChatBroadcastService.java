package com.streamerui.service;

import com.streamerui.model.ChatMessageDto;
import com.streamerui.model.OverlayConfig;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcasts to per-streamer WebSocket topics so each streamer's overlay
 * only sees their own chat/config, not anyone else's. The overlay page
 * subscribes to /topic/chat/{streamerId} and /topic/config/{streamerId},
 * where streamerId comes from the overlay URL (see PublicOverlayController).
 */
@Service
public class ChatBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastMessage(Long streamerId, ChatMessageDto message) {
        messagingTemplate.convertAndSend("/topic/chat/" + streamerId, message);
    }

    public void broadcastConfig(Long streamerId, OverlayConfig config) {
        messagingTemplate.convertAndSend("/topic/config/" + streamerId, config);
    }
}
