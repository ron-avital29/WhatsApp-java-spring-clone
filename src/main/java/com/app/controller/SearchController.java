package com.app.controller;

import com.app.model.Message;
import com.app.model.User;
import com.app.repo.MessageRepository;
import com.app.repo.UserRepository;
import com.app.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping
    public String searchAllMessagesAndUsers(@RequestParam("query") String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            return "redirect:/";
        }

        User currentUser = currentUserService.getCurrentAppUser();
        List<User> users = userRepository.searchNonAdminUsers(query).stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .toList();        model.addAttribute("users", users);

        model.addAttribute("query", query);

        List<Message> messagesInUsersChats = messageRepository.searchMessagesInUsersChats(query);
        model.addAttribute("messages", messagesInUsersChats);

        return "search";
    }

    @GetMapping("/start-convo-search")
    public String startConvoSearch(@RequestParam("query") String query, Model model) {
        List<User> users = userRepository.searchNonAdminUsers(query);
        model.addAttribute("users", users);
        model.addAttribute("query", query);

        return "start-conversation";
    }
}
