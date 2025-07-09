package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "googleId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

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

    public boolean isBanned() {
        return bannedUntil != null && bannedUntil.isAfter(LocalDateTime.now());
    }

    public String getBanDurationMessage() {
        if (bannedUntil == null) return "Not banned";
        long days = java.time.Duration.between(LocalDateTime.now(), bannedUntil).toDays();
        if (days >= 365 * 80) return "forever";
        if (days >= 7) return "1 week";
        if (days >= 1) return "24 hours";
        return "less than a day";
    }



}
