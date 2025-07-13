package com.app.service;

import com.app.model.BroadcastMessage;
import com.app.model.User;
import com.app.repo.BroadcastMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BroadcastService {

    private final BroadcastMessageRepository repository;

    public BroadcastService(BroadcastMessageRepository repository) {
        this.repository = repository;
    }

    public List<BroadcastMessage> getActiveMessages() {
        return repository.findByExpiresAtAfter(LocalDateTime.now());
    }

    public List<BroadcastMessage> getActiveMessagesByAdmin(User admin) {
        return repository.findByAdminAndExpiresAtAfter(admin, LocalDateTime.now());
    }

    public BroadcastMessage create(User admin, String content, LocalDateTime expiresAt) {
        BroadcastMessage msg = BroadcastMessage.builder()
                .admin(admin)
                .content(content)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        return repository.save(msg);
    }

    @Transactional
    public Optional<BroadcastMessage> updateContent(User admin, Long id, String newContent) {
        return repository.findById(id).filter(msg ->
                msg.getAdmin().equals(admin) && !msg.isExpired()
        ).map(msg -> {
            msg.setContent(newContent);
            return msg;
        });
    }

    public boolean delete(User admin, Long id) {
        return repository.findById(id).filter(msg ->
                msg.getAdmin().equals(admin) && !msg.isExpired()
        ).map(msg -> {
            repository.delete(msg);
            return true;
        }).orElse(false);
    }
}