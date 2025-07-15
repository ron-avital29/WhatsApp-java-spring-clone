package com.app.controller;

import com.app.model.BroadcastMessage;
import com.app.service.BroadcastService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * BroadcastRestController handles requests related to broadcast messages.
 * It retrieves active broadcast messages and returns them for display.
 */
@Controller
public class BroadcastRestController {

    /**
     * Service to handle broadcast operations.
     */
    @Autowired
    private BroadcastService broadcastService;

    /**
     * Retrieves active broadcast messages and adds them to the model for display.
     *
     * @param model the model to add attributes for the view
     * @param request the HTTP request
     * @return a fragment containing the broadcast messages
     */
    @GetMapping("/api/broadcasts")
    public String getBroadcasts(Model model, HttpServletRequest request) {
        List<BroadcastMessage> broadcasts = broadcastService.getActiveMessages();
        model.addAttribute("broadcasts", broadcasts);
        return "fragments/broadcast-card :: card";
    }
}
