package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a chatroom in the application.
 * A chatroom can be of different types: PRIVATE, GROUP, or COMMUNITY.
 * It contains information about the chatroom's name, type, members, and creator.
 */
@Entity
@Table(name = "chatrooms")
@Getter
@Setter
public class Chatroom implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatroomType type; // PRIVATE, GROUP, COMMUNITY

    @Column(nullable = false)
    private boolean isEditableName;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "user_chatrooms",
            joinColumns = @JoinColumn(name = "chatroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    /**
     * Adds a member to the chatroom.
     *
     * @param currentUser the user to be added as a member
     */
    @Transient
    public String getDisplayName(User currentUser) {
        if (type == ChatroomType.PRIVATE && name.contains(" & ")) {
            String[] names = name.split(" & ");
            for (String n : names) {
                if (!n.equalsIgnoreCase(currentUser.getUsername())) {
                    return n;
                }
            }
        }
        return name;
    }
}
