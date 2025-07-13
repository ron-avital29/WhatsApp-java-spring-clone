package com.app.repo;

import com.app.model.Report;
import com.app.model.ReportStatus;
import com.app.model.User;
import com.app.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndReportedMessage(User reporter, Message reportedMessage);

    @Query("SELECT DISTINCT r.reportedMessage FROM Report r WHERE r.status = 'PENDING'")
    List<Message> findDistinctReportedMessagesWithActiveReports();

    List<Report> findByReportedMessageAndStatusNot(Message message, ReportStatus status);

    List<Report> findAllByReporter(User user);

    List<Report> findByReportedMessageAndStatus(Message message, ReportStatus status);

    boolean existsByUpdatedAtAfter(LocalDateTime sinceTime);
}
