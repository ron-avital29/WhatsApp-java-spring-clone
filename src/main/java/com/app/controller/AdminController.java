package com.app.controller;

import com.app.dto.BannedUserDTO;
import com.app.dto.MessageReportDTO;
import com.app.model.Message;
import com.app.model.User;
import com.app.repo.UserRepository;
import com.app.service.AdminService;
import com.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminController handles administrative actions such as banning users,
 * dismissing message reports, and viewing reported messages.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Displays the admin panel with reported messages and performs cleanup of expired bans.
     *
     * @param model the model to add attributes for the view
     * @return the name of the admin panel view
     */
    @GetMapping("/panel")
    public String adminPanel(Model model) {
        model.addAttribute("reportedMessages", adminService.getReportedMessagesWithReportsAttached());
        adminService.cleanupExpiredBans();
        return "admin-panel";
    }

    /**
     * Displays the details of a specific message report.
     *
     * @param msgId the ID of the message to view
     * @param duration the model to add attributes for the view
     * @return the name of the message report view
     */
    @PostMapping("/ban-user/{msgId}")
    public String banUser(@PathVariable Long msgId, @RequestParam String duration) {
        userService.banUserByMessageId(msgId, duration);
        return "redirect:/admin/panel";
    }

    /**
     * Dismisses all reports associated with a specific message.
     *
     * @param messageId the ID of the message for which reports should be dismissed
     * @return a redirect to the admin panel
     */
    @PostMapping("/dismiss-message-reports/{messageId}")
    public String dismissAllReportsOnMessage(@PathVariable Long messageId) {
        adminService.dismissReportsForMessage(messageId);
        return "redirect:/admin/panel";
    }

    /**
     * Retrieves the latest reports since a specified time.
     *
     * @param since the time in ISO-8601 format to filter reports
     * @return a list of message report DTOs
     */
    @GetMapping("/panel/reports")
    @ResponseBody
    public List<MessageReportDTO> getLatestReports(@RequestParam(required = false) String since) {
        return adminService.getLatestReportsSince(since);
    }

    /**
     * Retrieves a list of users who are currently banned.
     *
     * @return a list of banned user DTOs
     */
    @GetMapping("/panel/banned-users")
    @ResponseBody
    public List<BannedUserDTO> getBannedUsers() {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByBannedUntilIsNotNullAndBannedUntilAfter(now)
                .stream()
                .map(user -> new BannedUserDTO(user.getId(), user.getUsername(), user.getBannedUntil()))
                .collect(Collectors.toList());
    }
}
