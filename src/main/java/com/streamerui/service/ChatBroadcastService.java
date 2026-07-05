package com.streamerui.service;

import com.streamerui.model.ChatMessageDto;
import com.streamerui.model.OverlayConfig;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastMessage(ChatMessageDto message) {
        messagingTemplate.convertAndSend("/topic/chat", message);
    }

    public void broadcastConfig(OverlayConfig config) {
        messagingTemplate.convertAndSend("/topic/config", config);
    }
}
