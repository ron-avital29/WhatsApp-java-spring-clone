package com.app.service;

import com.app.dto.MessageReportDTO;
import com.app.dto.ReportDTO;
import com.app.exception.ResourceNotFoundException;
import com.app.model.Message;
import com.app.model.Report;
import com.app.model.ReportStatus;
import com.app.model.User;
import com.app.repo.MessageRepository;
import com.app.repo.ReportRepository;
import com.app.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AdminService provides administrative functionalities such as cleaning up expired bans,
 * dismissing reports for messages, and retrieving reports.
 * It interacts with repositories to perform database operations.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    /**
     * Repositories for accessing message, report, and user data.
     * These are injected by Spring's dependency injection mechanism.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Repository for accessing report data.
     * It provides methods to find reports based on various criteria.
     */
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Repository for accessing user data.
     * It provides methods to find users with expired bans.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Cleans up expired bans for users.
     * It retrieves users whose bans have expired and removes the ban status.
     * The method updates the user records in the database.
     */
    public void cleanupExpiredBans() {
        LocalDateTime now = LocalDateTime.now();
        List<User> expiredBans = userRepository.findUsersWithExpiredBans(now);

        for (User user : expiredBans) {
            user.setBannedUntil(null);
            userRepository.save(user);
        }
    }

    /**
     * Dismisses all reports for a specific message.
     * It finds the message by its ID, retrieves all active reports for that message,
     * and updates their status to DISMISSED.
     * The method saves the updated reports back to the database.
     *
     * @param messageId the ID of the message for which reports should be dismissed
     */
    public void dismissReportsForMessage(Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message with ID " + messageId + " not found."));

        List<Report> reports = reportRepository.findByReportedMessageAndStatusNot(message, ReportStatus.DISMISSED);
        LocalDateTime now = LocalDateTime.now();

        for (Report r : reports) {
            r.setStatus(ReportStatus.DISMISSED);
            r.setUpdatedAt(now);
        }

        reportRepository.saveAll(reports);
    }

    /**
     * Retrieves the latest reports since a specified time.
     * It parses the provided timestamp, checks if there are any reports updated after that time,
     * and collects details of reported messages and their active reports.
     * The method returns a list of MessageReportDTO objects containing report details.
     *
     * @param since the timestamp in ISO format to filter reports
     * @return a list of MessageReportDTO containing report details
     */
    public List<MessageReportDTO> getLatestReportsSince(String since) {
        List<MessageReportDTO> dtos = new ArrayList<>();
        LocalDateTime sinceTime;

        try {
            sinceTime = Instant.parse(since).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return dtos;
        }

        boolean anyChanged = reportRepository.existsByUpdatedAtAfter(sinceTime);
        if (!anyChanged) return dtos;

        List<Message> reportedMessages = reportRepository.findDistinctReportedMessagesWithActiveReports();

        for (Message msg : reportedMessages) {
            List<Report> activeReports = reportRepository.findByReportedMessageAndStatusNot(msg, ReportStatus.DISMISSED);

            MessageReportDTO dto = new MessageReportDTO();
            dto.setId(msg.getId());
            dto.setContent(msg.getContent());
            dto.setSenderUsername(msg.getSender().getUsername());

            if (msg.getSender().getBannedUntil() != null) {
                dto.setBannedUntil(msg.getSender().getBannedUntil().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
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

            LocalDateTime latestUpdate = activeReports.stream()
                    .map(Report::getUpdatedAt)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());

            dto.setLastUpdated(latestUpdate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * Retrieves all reported messages with their active reports attached.
     * It finds distinct reported messages that have active reports,
     * and for each message, it retrieves the associated active reports.
     * The method returns a list of Message objects with their reports populated.
     *
     * @return a list of Message objects with active reports attached
     */
    public List<Message> getReportedMessagesWithReportsAttached() {
        List<Message> reportedMessages = reportRepository.findDistinctReportedMessagesWithActiveReports();
        for (Message msg : reportedMessages) {
            List<Report> activeReports = reportRepository.findByReportedMessageAndStatusNot(msg, ReportStatus.DISMISSED);
            msg.setReports(activeReports);
        }
        return reportedMessages;
    }
}
