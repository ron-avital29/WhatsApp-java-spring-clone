package com.app.controller;

import com.app.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/")
    public String index() {
        return "index"; // Public landing page
    }

    @GetMapping("/home")
    public String home(Model model) {
        System.out.println("going to home");
        OAuth2User user = currentUserService.getCurrentUser();
        if (user != null) {
            model.addAttribute("name", user.getAttribute("name"));
            model.addAttribute("email", user.getAttribute("email"));
        }
        return "home";
    }

    @GetMapping("/logout")
    public String confirmLogout() {
        return "logout-confirm"; // renders logout-confirm.html
    }

}
