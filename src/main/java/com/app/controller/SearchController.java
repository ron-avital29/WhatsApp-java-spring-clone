package com.app.controller;

import com.app.model.User;
import com.app.repo.UserRepository;
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

    @GetMapping
    public String searchUsers(@RequestParam("query") String query, Model model) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        model.addAttribute("users", users);
        model.addAttribute("query", query);

        // Later:
        // model.addAttribute("groups", ...);
        // model.addAttribute("messages", ...);
        // model.addAttribute("communities", ...);

        return "search";
    }
}
