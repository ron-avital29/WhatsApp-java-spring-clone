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

@Component
public class WebSocketEventListener {

    private final Map<Long, Set<String>> chatroomUsers = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionChatroomMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    private Long extractChatroomId(StompHeaderAccessor accessor) {
        String idHeader = accessor.getFirstNativeHeader("chatroomId");
        return (idHeader != null) ? Long.parseLong(idHeader) : null;
    }

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
