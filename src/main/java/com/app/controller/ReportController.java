package com.app.controller;

import com.app.model.Chatroom;
import com.app.model.Message;
import com.app.model.Report;
import com.app.model.User;
import com.app.repo.MessageRepository;
import com.app.repo.ReportRepository;
import com.app.service.CurrentUserService;
import com.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * ReportController handles reporting messages in chatrooms.
 * It provides endpoints to show the report form, submit reports,
 * and display a thank you page after a report is submitted.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    /**
     * Service to handle report-related operations.
     */
    @Autowired
    private ReportService reportService;

    /**
     * Repository to access message data.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Repository to access report data.
     */
    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Repository to access report data.
     */
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Displays the report form for a specific message.
     * Checks if the user is logged in, if the message exists,
     * and if the user is a member of the chatroom.
     * If the message was sent by the user, redirects to the chatroom view.
     * If the user has already reported the message, redirects to the chatroom view.
     *
     * @param messageId the ID of the message to report
     * @param model     the model to add attributes for the view
     * @return the name of the report form view or a redirect URL
     */
    @GetMapping("/message/{messageId}")
    public String showReportForm(@PathVariable Long messageId, Model model) {
        User currentUser = currentUserService.getCurrentAppUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return "redirect:/home";
        }

        Message message = messageOpt.get();
        Long chatroomId = message.getChatroom().getId();

        Chatroom chatroom = message.getChatroom();
        if (!chatroom.getMembers().contains(currentUser)) {
            return "redirect:/chatrooms/" + chatroomId + "/view-chatroom";
        }

        if (message.getSender().getId().equals(currentUser.getId())) {
            return "redirect:/chatrooms/" + chatroomId + "/view-chatroom";
        }

        boolean alreadyReported = reportRepository.existsByReporterAndReportedMessage(currentUser, message);
        if (alreadyReported) {
            return "redirect:/chatrooms/" + chatroomId + "/view-chatroom";
        }

        model.addAttribute("message", message);
        return "report-form";
    }

    /**
     * Submits a report for a specific message.
     * Checks if the user is logged in, if the message exists,
     * and submits the report with the provided reason.
     * Redirects to a thank you page after successful submission.
     *
     * @param messageId the ID of the message to report
     * @param reason    the reason for reporting the message
     * @param model     the model to add attributes for the view
     * @return a redirect URL to the thank you page or chatroom view
     */
    @PostMapping("/message/{messageId}")
    public String submitMessageReport(
            @PathVariable Long messageId,
            @RequestParam("reason") String reason,
            Model model
    ) {
        User currentUser = currentUserService.getCurrentAppUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return "redirect:/home";
        }

        Message message = messageOpt.get();
        Long chatroomId = message.getChatroom().getId();

        Optional<Report> reportOpt = reportService.submitMessageReport(currentUser, message, reason);
        if (reportOpt.isEmpty()) {
            return "redirect:/chatrooms/" + chatroomId + "/view-chatroom";
        }

        return "redirect:/reports/thank-you?chatroomId=" + chatroomId;
    }

    /**
     * Displays a thank you page after a report is successfully submitted.
     * Adds the chatroom ID to the model for further use in the view.
     *
     * @param chatroomId the ID of the chatroom where the report was submitted
     * @param model      the model to add attributes for the view
     * @return the name of the thank you view
     */
    @GetMapping("/thank-you")
    public String showThankYouPage(@RequestParam Long chatroomId, Model model) {
        model.addAttribute("chatroomId", chatroomId);
        return "report-thankyou";
    }
}
