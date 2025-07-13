package com.app.controller;

import com.app.model.BroadcastMessage;
import com.app.service.BroadcastService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BroadcastRestController {

    private final BroadcastService broadcastService;

    public BroadcastRestController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @GetMapping("/api/broadcasts")
    public String getBroadcasts(Model model, HttpServletRequest request) {
        List<BroadcastMessage> broadcasts = broadcastService.getActiveMessages();
        model.addAttribute("broadcasts", broadcasts);
        return "fragments/broadcast-card :: card";
    }
}
