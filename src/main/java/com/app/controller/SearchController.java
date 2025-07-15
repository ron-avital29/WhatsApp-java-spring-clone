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

/**
 * SearchController handles search operations for messages and users.
 * It provides endpoints to search for messages in users' chats and to find users based on a query.
 */
@Controller
@RequestMapping("/search")
public class SearchController {

    /**
     * Repository to access user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Repository to access message data.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Service to retrieve the current user's information.
     */
    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Searches for messages and users based on the provided query.
     * If the query is empty or null, redirects to the home page.
     * Otherwise, retrieves non-admin users excluding the current user,
     * and searches for messages in the current user's chats.
     *
     * @param query the search query
     * @param model the model to add attributes for the view
     * @return the name of the search view
     */
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

    /**
     * Starts a conversation search based on the provided query.
     * Retrieves non-admin users matching the query and adds them to the model.
     *
     * @param query the search query
     * @param model the model to add attributes for the view
     * @return the name of the start conversation view
     */
    @GetMapping("/start-convo-search")
    public String startConvoSearch(@RequestParam("query") String query, Model model) {
        List<User> users = userRepository.searchNonAdminUsers(query);
        model.addAttribute("users", users);
        model.addAttribute("query", query);

        return "start-conversation";
    }
}
