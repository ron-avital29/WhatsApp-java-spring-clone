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

@Service
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public void cleanupExpiredBans() {
        LocalDateTime now = LocalDateTime.now();
        List<User> expiredBans = userRepository.findUsersWithExpiredBans(now);

        for (User user : expiredBans) {
            user.setBannedUntil(null);
            userRepository.save(user);
        }
    }

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

    public List<Message> getReportedMessagesWithReportsAttached() {
        List<Message> reportedMessages = reportRepository.findDistinctReportedMessagesWithActiveReports();
        for (Message msg : reportedMessages) {
            List<Report> activeReports = reportRepository.findByReportedMessageAndStatusNot(msg, ReportStatus.DISMISSED);
            msg.setReports(activeReports);
        }
        return reportedMessages;
    }
}
