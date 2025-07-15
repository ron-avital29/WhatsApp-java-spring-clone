package com.app.repo;

import com.app.model.BroadcastMessage;
import com.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing BroadcastMessage entities.
 * Provides methods to find active broadcast messages and those by a specific admin.
 */
@Repository
public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, Long> {
    List<BroadcastMessage> findByExpiresAtAfter(LocalDateTime now);
    List<BroadcastMessage> findByAdminAndExpiresAtAfter(User admin, LocalDateTime now);
}