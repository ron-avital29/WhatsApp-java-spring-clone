package com.app.controller;

import com.app.dto.BannedUserDTO;
import com.app.dto.MessageReportDTO;
import com.app.dto.ReportDTO;
import com.app.model.Message;
import com.app.model.Report;
import com.app.model.ReportStatus;
import com.app.model.User;
import com.app.repo.MessageRepository;
import com.app.repo.ReportRepository;
import com.app.repo.UserRepository;
import com.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/panel")
    public String adminPanel(Model model) {
        System.out.println("entering adminPanel");

        List<Message> reportedMessages = reportRepository.findDistinctReportedMessagesWithActiveReports();

        for (Message msg : reportedMessages) {
            List<Report> activeReports = reportRepository.findByReportedMessageAndStatusNot(msg, ReportStatus.DISMISSED);
            msg.setReports(activeReports);
        }

        model.addAttribute("reportedMessages", reportedMessages);

        cleanupExpiredBans();

        return "admin-panel";
    }

    @PostMapping("/block-user/{userId}")
    public String blockUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole("BLOCKED");
        userRepository.save(user);
        return "redirect:/admin/panel";
    }

    @PostMapping("/ban-user/{msgId}")
    public String banUser(@PathVariable Long msgId, @RequestParam String duration) {
        System.out.println("Banning user for message ID: " + msgId + " with duration: " + duration);
        userService.banUserByMessageId(msgId, duration);
        return "redirect:/admin/panel";
    }

    @PostMapping("/dismiss-report/{reportId}")
    public String dismissReport(@PathVariable Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow();
        report.setStatus(ReportStatus.DISMISSED);
        reportRepository.save(report);
        return "redirect:/admin/panel";
    }

    @PostMapping("/dismiss-message-reports/{messageId}")
    public String dismissAllReportsOnMessage(@PathVariable Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow();
        List<Report> reports = reportRepository.findByReportedMessageAndStatusNot(message, ReportStatus.DISMISSED);

        for (Report r : reports) {
            r.setStatus(ReportStatus.DISMISSED);
        }

        reportRepository.saveAll(reports);
        return "redirect:/admin/panel";
    }

//    @GetMapping("/panel/banned-users")
//    @ResponseBody
//    public List<BannedUserDTO> getBannedUsers() {
//        return userRepository.findAll().stream()
//                .filter(user -> user.getBannedUntil() != null)
//                .map(user -> new BannedUserDTO(user.getId(), user.getUsername(), user.getBannedUntil()))
//                .toList();
//    }

    @GetMapping("/panel/reports")
    @ResponseBody
    public List<MessageReportDTO> getLatestReports() {
        List<Message> reportedMessages = reportRepository.findDistinctReportedMessagesWithActiveReports();

        List<MessageReportDTO> dtos = new ArrayList<>();

        for (Message msg : reportedMessages) {
            List<Report> activeReports = reportRepository.findByReportedMessageAndStatusNot(msg, ReportStatus.DISMISSED);

            MessageReportDTO dto = new MessageReportDTO();
            dto.setId(msg.getId());
            dto.setContent(msg.getContent());
            dto.setSenderUsername(msg.getSender().getUsername());

            if (msg.getSender().getBannedUntil() != null) {
                String formatted = msg.getSender().getBannedUntil().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                dto.setBannedUntil(formatted);
            }

            if (msg.getFile() != null) {
                dto.setFileId(msg.getFile().getId());
                dto.setFileName(msg.getFile().getFilename());
                dto.setFileMimeType(msg.getFile().getMimeType());
            }

            List<ReportDTO> reportDTOs = new ArrayList<>();
            for (Report report : activeReports) {
                ReportDTO r = new ReportDTO();
                r.setReporterUsername(report.getReporter().getUsername());
                r.setReason(report.getReason());
                reportDTOs.add(r);
            }

            dto.setReports(reportDTOs);
            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * AJAX endpoint to get currently banned users
     */
    @GetMapping("/panel/banned-users")
    @ResponseBody
    public List<BannedUserDTO> getBannedUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> bannedUsers = userRepository.findByBannedUntilIsNotNullAndBannedUntilAfter(now);

        return bannedUsers.stream()
                .map(user -> new BannedUserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getBannedUntil()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Clean up expired bans by setting banned_until to null
     */
    private void cleanupExpiredBans() {
        LocalDateTime now = LocalDateTime.now();
        List<User> expiredBans = userRepository.findUsersWithExpiredBans(now);

        for (User user : expiredBans) {
            user.setBannedUntil(null);
            userRepository.save(user);
        }
    }
}