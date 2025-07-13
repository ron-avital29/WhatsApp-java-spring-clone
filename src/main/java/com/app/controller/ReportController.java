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

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ReportRepository reportRepository;

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

    @GetMapping("/thank-you")
    public String showThankYouPage(@RequestParam Long chatroomId, Model model) {
        model.addAttribute("chatroomId", chatroomId);
        return "report-thankyou";
    }
}
