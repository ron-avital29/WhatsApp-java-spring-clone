package com.app.service;

import com.app.model.BroadcastMessage;
import com.app.model.User;
import com.app.repo.BroadcastMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BroadcastService provides methods to manage broadcast messages.
 * It allows creating, updating, deleting, and retrieving active messages.
 */
@Service
public class BroadcastService {

    /**
     * Repository to access broadcast messages.
     * It provides methods to find messages based on expiration time and admin.
     */
    @Autowired
    private BroadcastMessageRepository repository;

    /**
     * Retrieves all active broadcast messages.
     * An active message is one that has not expired.
     *
     * @return a list of active broadcast messages
     */
    public List<BroadcastMessage> getActiveMessages() {
        return repository.findByExpiresAtAfter(LocalDateTime.now());
    }

    /**
     * Retrieves active broadcast messages created by a specific admin.
     * An active message is one that has not expired.
     *
     * @param admin the admin who created the messages
     * @return a list of active broadcast messages created by the specified admin
     */
    public List<BroadcastMessage> getActiveMessagesByAdmin(User admin) {
        return repository.findByAdminAndExpiresAtAfter(admin, LocalDateTime.now());
    }

    /**
     * Creates a new broadcast message.
     * The message is created with the current time as the creation time
     * and the specified expiration time.
     *
     * @param admin     the admin creating the message
     * @param content   the content of the message
     * @param expiresAt the expiration time of the message
     * @return the created broadcast message
     */
    public synchronized BroadcastMessage create(User admin, String content, LocalDateTime expiresAt) {
        BroadcastMessage msg = BroadcastMessage.builder()
                .admin(admin)
                .content(content)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        return repository.save(msg);
    }

    /**
     * Updates the content of an existing broadcast message.
     * The message can only be updated if it was created by the specified admin
     * and has not expired.
     *
     * @param admin      the admin updating the message
     * @param id         the ID of the message to update
     * @param newContent the new content for the message
     */
    @Transactional
    public synchronized void updateContent(User admin, Long id, String newContent) {
        repository.findById(id).filter(msg ->
                msg.getAdmin().equals(admin) && !msg.isExpired()
        ).ifPresent(msg -> msg.setContent(newContent));
    }

    /**
     * Deletes a broadcast message.
     * The message can only be deleted if it was created by the specified admin
     * and has not expired.
     *
     * @param admin the admin deleting the message
     * @param id    the ID of the message to delete
     */
    public synchronized void delete(User admin, Long id) {
        repository.findById(id).filter(msg ->
                msg.getAdmin().equals(admin) && !msg.isExpired()
        ).ifPresent(repository::delete);
    }
}
