package com.app.service;

import com.app.model.*;
import com.app.repo.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

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

    private boolean isUserInChatroom(User user, Chatroom chatroom) {
        return chatroom.getMembers().contains(user);
    }
}
