package com.app.service;

import com.app.model.*;
import com.app.repo.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ReportService handles the submission of reports for messages in chatrooms.
 * It checks if the reporter is a member of the chatroom and if they have already reported the message.
 * If both conditions are met, it creates a new report and saves it to the repository.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    /**
     * Repository for managing reports.
     */
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Submits a report for a message in a chatroom.
     *
     * @param reporter the user submitting the report
     * @param message the message being reported
     * @param reason the reason for reporting the message
     * @return an Optional containing the saved Report if successful, or empty if conditions are not met
     */
    @Transactional
    public Optional<Report> submitMessageReport(User reporter, Message message, String reason) {
        if (!isUserInChatroom(reporter, message.getChatroom())) {
            return Optional.empty();
        }

        if (reportRepository.existsByReporterAndReportedMessage(reporter, message)) {
            return Optional.empty();
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportedMessage(message)
                .reportedUser(message.getSender())
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .status(ReportStatus.PENDING)
                .build();

        return Optional.of(reportRepository.save(report));
    }

    /**
     * Checks if a user is a member of a chatroom.
     *
     * @param user the user to check
     * @param chatroom the chatroom to check against
     * @return true if the user is a member of the chatroom, false otherwise
     */
    private boolean isUserInChatroom(User user, Chatroom chatroom) {
        return chatroom.getMembers().contains(user);
    }
}
