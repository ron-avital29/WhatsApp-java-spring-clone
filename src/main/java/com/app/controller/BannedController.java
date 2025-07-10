package com.app.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
public class BannedController {

    @GetMapping("/banned")
    public String bannedPage(HttpSession session, Model model) {
        System.out.println("BannedController: Accessing banned page");

        LocalDateTime bannedUntil = (LocalDateTime) session.getAttribute("bannedUntil");

        if (bannedUntil == null) {
            return "redirect:/login";
        }

        model.addAttribute("bannedUntil", bannedUntil);
        session.removeAttribute("bannedUntil");

        return "banned";
    }
}
