package com.app.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

/**
 * Controller to handle banned user access.
 * Displays a banned page with the duration of the ban.
 */
@Controller
public class BannedController {

    /**
     * Displays the banned page if the user is banned.
     * Retrieves the ban duration from the session and displays it.
     *
     * @param session the HTTP session containing ban information
     * @param model   the model to add attributes for the view
     * @return the name of the banned view or redirects to login if not banned
     */
    @GetMapping("/banned")
    public String bannedPage(HttpSession session, Model model) {

        LocalDateTime bannedUntil = (LocalDateTime) session.getAttribute("bannedUntil");

        if (bannedUntil == null) {
            return "redirect:/login";
        }

        model.addAttribute("bannedUntil", bannedUntil);
        session.removeAttribute("bannedUntil");

        return "banned";
    }
}
