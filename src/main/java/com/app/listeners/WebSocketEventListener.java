package com.app.listeners;

import com.app.dto.PresenceMessage;
import com.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketEventListener listens for WebSocket connection and disconnection events.
 * It manages the presence of users in chatrooms, sending notifications when users join or leave.
 */
@Component
public class WebSocketEventListener {

    /**
     * A map that holds the users connected to each chatroom.
     * The key is the chatroom ID, and the value is a set of user IDs.
     */
    private final Map<Long, Set<String>> chatroomUsers = new ConcurrentHashMap<>();
    /**
     * A map that associates session IDs with chatroom IDs.
     * This helps track which session belongs to which chatroom.
     */
    private final Map<String, Long> sessionChatroomMap = new ConcurrentHashMap<>();

    /**
     * Messaging template for sending messages to WebSocket clients.
     * It is used to broadcast presence messages when users join or leave chatrooms.
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Service to handle user-related operations, such as retrieving display names.
     */
    @Autowired
    private UserService userService;

    /**
     * Extracts the chatroom ID from the StompHeaderAccessor.
     * If the header is not present, it returns null.
     *
     * @param accessor the StompHeaderAccessor containing headers
     * @return the chatroom ID or null if not found
     */
    private Long extractChatroomId(StompHeaderAccessor accessor) {
        String idHeader = accessor.getFirstNativeHeader("chatroomId");
        return (idHeader != null) ? Long.parseLong(idHeader) : null;
    }

    /**
     * Handles WebSocket connection events.
     * When a user connects, it adds them to the chatroom and notifies other users.
     *
     * @param event the session connected event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        Long chatroomId = extractChatroomId(accessor);

        if (user != null && chatroomId != null) {
            String userId = user.getName();
            String sessionId = accessor.getSessionId();

            synchronized (chatroomUsers) {
                chatroomUsers.computeIfAbsent(chatroomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
            }

            sessionChatroomMap.put(sessionId, chatroomId);

            String username = userService.getDisplayNameByGoogleId(userId);
            messagingTemplate.convertAndSend("/topic/presence/" + chatroomId,
                    new PresenceMessage(username, "JOIN"));
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        String sessionId = accessor.getSessionId();
        Long chatroomId = sessionChatroomMap.remove(sessionId);

        if (user != null && chatroomId != null) {
            String userId = user.getName();

            synchronized (chatroomUsers) {
                Set<String> users = chatroomUsers.get(chatroomId);
                if (users != null) {
                    users.remove(userId);
                    if (users.isEmpty()) {
                        chatroomUsers.remove(chatroomId);
                    }
                }
            }

            String username = userService.getDisplayNameByGoogleId(userId);
            messagingTemplate.convertAndSend("/topic/presence/" + chatroomId,
                    new PresenceMessage(username, "LEAVE"));
        }
    }

    public Set<String> getConnectedUsers(Long chatroomId) {
        return chatroomUsers.getOrDefault(chatroomId, Collections.emptySet());
    }

    public void addUserToChatroom(String userId, Long chatroomId, String sessionId) {
        synchronized (chatroomUsers) {
            chatroomUsers.computeIfAbsent(chatroomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        }
        sessionChatroomMap.put(sessionId, chatroomId);

        String username = userService.getDisplayNameByGoogleId(userId);
        messagingTemplate.convertAndSend("/topic/presence/" + chatroomId,
                new PresenceMessage(username, "JOIN"));
    }
}
