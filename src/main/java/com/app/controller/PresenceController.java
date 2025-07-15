package com.app.controller;

import com.app.listeners.WebSocketEventListener;
import com.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PresenceController provides endpoints to retrieve online users in a chatroom.
 * It uses WebSocketEventListener to get the list of connected users and UserService
 * to map Google IDs to display names.
 */
@RestController
@RequestMapping("/presence")
public class PresenceController {

    /**
     * Listener to handle WebSocket events and track connected users.
     */
    @Autowired
    private WebSocketEventListener listener;

    /**
     * Service to handle user-related operations, such as retrieving display names.
     */
    @Autowired
    private UserService userService;

    /**
     * Retrieves the list of online users in a specific chatroom.
     * The chatroom ID is provided as a path variable.
     *
     * @param id the ID of the chatroom
     * @return a set of display names of online users in the chatroom
     */
    @GetMapping("/online/chatroom/{id}")
    public Set<String> getOnlineUsers(@PathVariable Long id) {
        return listener.getConnectedUsers(id).stream()
                .map(userService::getDisplayNameByGoogleId)
                .collect(Collectors.toSet());
    }
}
