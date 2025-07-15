package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * User represents a user in the application.
 * It contains details such as username, email, password, role, and associated chatrooms.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "googleId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String username;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false)
    private String role; // "USER" or "ADMIN"

    @Column(nullable = false, unique = true)
    private String googleId;

    private String avatarId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "members")
    private Set<Chatroom> chatrooms = new HashSet<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    private List<Report> reportsMade;

    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL)
    private List<Report> reportsReceived;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    /**
     * Checks if the user is currently banned.
     * A user is considered banned if the bannedUntil date is in the future.
     *
     * @return true if the user is banned, false otherwise
     */
    public boolean isBanned() {
        return bannedUntil != null && bannedUntil.isAfter(LocalDateTime.now());
    }
}
