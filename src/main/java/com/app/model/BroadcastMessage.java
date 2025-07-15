package com.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BroadcastMessage represents a message sent by an admin to all users.
 * It contains the content of the message, the admin who sent it, and timestamps for creation and expiration.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Checks if the broadcast message has expired.
     *
     * @return true if the message is expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}