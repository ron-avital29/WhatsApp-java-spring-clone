package com.app.controller;

import com.app.model.User;
import com.app.service.BroadcastService;
import com.app.service.CurrentUserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for managing broadcast messages in the admin panel.
 * Provides endpoints to create, edit, delete, and view broadcasts.
 */
@Controller
@RequestMapping("/admin/broadcast")
public class BroadcastController {

    @Autowired
    private BroadcastService broadcastService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Displays the broadcast management page with active broadcasts.
     *
     * @param model the model to add attributes for the view
     * @return the name of the broadcast management view
     */
    @GetMapping("/manage")
    public String manageBroadcasts(Model model) {
        User admin = currentUserService.getCurrentAppUser();
        model.addAttribute("broadcasts", broadcastService.getActiveMessagesByAdmin(admin));
        return "broadcast-manage";
    }

    /**
     * Displays the broadcast creation form.
     *
     * @return the name of the broadcast creation view
     */
    @PostMapping("/create")
    public String create(@RequestParam @NotBlank String content,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt) {
        broadcastService.create(currentUserService.getCurrentAppUser(), content, expiresAt);
        return "redirect:/admin/broadcast/manage";
    }

    /**
     * Displays the broadcast editing form.
     *
     * @param id the ID of the broadcast to edit
     * @param content the model to add attributes for the view
     * @return the name of the broadcast edit view
     */
    @PostMapping("/edit")
    public String edit(@RequestParam Long id,
                       @RequestParam String content) {
        broadcastService.updateContent(currentUserService.getCurrentAppUser(), id, content);
        return "redirect:/admin/broadcast/manage";
    }

    /**
     * Deletes a broadcast message.
     *
     * @param id the ID of the broadcast to delete
     * @return a redirect to the broadcast management page
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        broadcastService.delete(currentUserService.getCurrentAppUser(), id);
        return "redirect:/admin/broadcast/manage";
    }
}