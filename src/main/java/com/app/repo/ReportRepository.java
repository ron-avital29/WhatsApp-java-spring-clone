package com.app.repo;

import com.app.model.Report;
import com.app.model.ReportStatus;
import com.app.model.User;
import com.app.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing Report entities.
 * Provides methods to check for existing reports, find reported messages,
 * and retrieve reports based on various criteria.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Checks if a report exists for the given reporter and reported message.
     *
     * @param reporter         the user who reported the message
     * @param reportedMessage  the message that was reported
     * @return true if a report exists, false otherwise
     */
    boolean existsByReporterAndReportedMessage(User reporter, Message reportedMessage);

    @Query("SELECT DISTINCT r.reportedMessage FROM Report r WHERE r.status = 'PENDING'")
    List<Message> findDistinctReportedMessagesWithActiveReports();

    List<Report> findByReportedMessageAndStatusNot(Message message, ReportStatus status);

    List<Report> findAllByReporter(User user);

    List<Report> findByReportedMessageAndStatus(Message message, ReportStatus status);

    /**
     * Checks if there are any reports that were updated after the specified time.
     *
     * @param sinceTime the time to check against
     * @return true if there are reports updated after the specified time, false otherwise
     */
    boolean existsByUpdatedAtAfter(LocalDateTime sinceTime);
}
