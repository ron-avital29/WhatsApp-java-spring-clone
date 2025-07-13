package com.app.controller;

import com.app.model.BroadcastMessage;
import com.app.model.Chatroom;
import com.app.repo.ChatroomRepository;
import com.app.service.BroadcastService;
import com.app.service.CurrentUserService;
import com.app.session.UserSessionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserSessionBean userSession;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private BroadcastService broadcastService;

    @GetMapping("/")
    public String index() {
        return "DELETE_ME_index";
    }

    @GetMapping("/home")
    public String home(Model model) {
        OAuth2User user = currentUserService.getCurrentUser();
        if (user != null) {
            model.addAttribute("name", user.getAttribute("name"));
            model.addAttribute("email", user.getAttribute("email"));
        }

        List<Long> recentIds = userSession.getRecentChatrooms();
        List<Chatroom> recentChatrooms = chatroomRepository.findAllById(recentIds);
        model.addAttribute("recentChatrooms", recentChatrooms);

        List<BroadcastMessage> broadcasts = broadcastService.getActiveMessages();
        model.addAttribute("broadcasts", broadcasts);

        return "home";
    }

    @GetMapping("/logout")
    public String confirmLogout() {
        return "logout-confirm";
    }
}
