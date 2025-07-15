package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;

/**
 * PresenceMessage represents a user's presence in the application.
 * It contains the username and the type of presence event (JOIN or LEAVE).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String type; // "JOIN" or "LEAVE"
}
