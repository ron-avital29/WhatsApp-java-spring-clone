package com.app.controller;

import com.app.dto.ChatMessageDTO;
import com.app.listeners.WebSocketEventListener;
import com.app.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;

/**
 * WebsocketController handles WebSocket messages for chat functionality.
 * It allows users to send chat messages and join chatrooms.
 */
@Controller
public class WebsocketController {

    /**
     * Template for sending messages to WebSocket clients.
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Service to handle chat message operations.
     * It processes incoming messages and manages chatrooms.
     */
    @Autowired
    private MessageService messageService;

    /**
     * Listener for WebSocket events.
     * It manages user sessions and chatroom memberships.
     */
    @Autowired
    private WebSocketEventListener webSocketEventListener;

    /**
     * Handles incoming chat messages.
     * When a message is received, it processes the message and sends it to the appropriate chatroom.
     *
     * @param message the chat message to be sent
     */
    @MessageMapping("/chat")
    public void send(ChatMessageDTO message) {
        ChatMessageDTO updated = messageService.sendChatMessage(message);
        messagingTemplate.convertAndSend("/topic/messages/" + updated.getChatroomId(), updated);
    }

    /**
     * Handles user joining a chatroom.
     * When a user joins, it adds them to the chatroom and notifies other users.
     *
     * @param payload   contains the chatroom ID
     * @param accessor  provides access to WebSocket session information
     */
    @MessageMapping("/join")
    public void handleJoin(Map<String, Long> payload, StompHeaderAccessor accessor) {
        Long chatroomId = payload.get("chatroomId");
        Principal user = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (user != null && chatroomId != null) {
            webSocketEventListener.addUserToChatroom(user.getName(), chatroomId, sessionId);
        }
    }
}
