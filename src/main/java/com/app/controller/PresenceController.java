package com.app.controller;

import com.app.service.UserService;
import com.app.service.CurrentUserService;
import com.app.listeners.WebSocketEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PresenceController provides endpoints to retrieve online users in a chatroom.
 */
@RestController
@RequestMapping("/presence")
public class PresenceController {

    @Autowired
    private WebSocketEventListener listener;

    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Retrieves the list of online users in a specific chatroom, excluding the current user.
     *
     * @param id the ID of the chatroom
     * @return a set of display names of online users
     */
    @GetMapping("/online/chatroom/{id}")
    public Set<String> getOnlineUsers(@PathVariable Long id) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        String currentUserGoogleId = currentUser.getAttribute("sub");

        return listener.getConnectedUsers(id).stream()
                .filter(googleId -> !googleId.equals(currentUserGoogleId))
                .map(userService::getDisplayNameByGoogleId)
                .collect(Collectors.toSet());
    }
}
