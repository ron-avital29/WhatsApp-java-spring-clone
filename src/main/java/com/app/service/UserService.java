package com.app.service;

import com.app.model.Message;
import com.app.model.Report;
import com.app.model.ReportStatus;
import com.app.model.User;
import com.app.repo.MessageRepository;
import com.app.repo.ReportRepository;
import com.app.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserService provides methods to manage user-related operations such as finding users by Google ID,
 * banning users, and retrieving display names.
 * It interacts with UserRepository, ReportRepository, and MessageRepository to perform these operations.
 */
@Service
public class UserService {

    /**
     * Repositories for accessing user, report, and message data.
     * These are injected by Spring's dependency injection mechanism.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Repository for managing reports related to messages.
     * It is used to update the status of reports when a user is banned.
     */
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Repository for managing messages sent by users.
     * It is used to find the user associated with a specific message when banning a user.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Finds a user by their Google ID.
     *
     * @param googleId the Google ID of the user
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    /**
     * Retrieves the display name of a user based on their Google ID.
     * If the user is not found, it returns "Unknown User".
     *
     * @param googleId the Google ID of the user
     * @return the display name of the user or "Unknown User" if not found
     */
    public String getDisplayNameByGoogleId(String googleId) {
        return findByGoogleId(googleId)
                .map(User::getUsername)
                .orElse("Unknown User");
    }

    /**
     * Bans a user based on the message ID of a report.
     * The ban duration can be specified as "24h", "1w", or "permanent".
     * If the user is already banned, it extends the ban if the requested duration is longer.
     * It also updates the status of related reports to indicate action has been taken.
     *
     * @param messageId the ID of the message associated with the report
     * @param duration  the duration of the ban ("24h", "1w", or "permanent")
     */
    public void banUserByMessageId(Long messageId, String duration) {
        Message message = messageRepository.findById(messageId).orElseThrow();
        User user = message.getSender();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requestedUntil = switch (duration) {
            case "24h" -> now.plusHours(24);
            case "1w" -> now.plusDays(7);
            default -> now.plusYears(100);
        };

        if (user.getBannedUntil() == null || user.getBannedUntil().isBefore(requestedUntil)) {
            user.setBannedUntil(requestedUntil);
            userRepository.save(user);
        }

        List<Report> relatedReports = reportRepository.findByReportedMessageAndStatus(message, ReportStatus.PENDING);
        for (Report report : relatedReports) {
            report.setStatus(ReportStatus.ACTION_TAKEN);
            report.setUpdatedAt(LocalDateTime.now());
        }
        reportRepository.saveAll(relatedReports);
    }
}
