package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BannedUserDTO represents a user who has been banned from the application.
 * It contains the user's ID, username, and the date until which they are banned.
 */
@Data
@AllArgsConstructor
public class BannedUserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private LocalDateTime bannedUntil;
}
