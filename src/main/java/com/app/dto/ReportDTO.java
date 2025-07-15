package com.app.dto;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * ChatMessageDTO represents a message in a chat application.
 * It contains details about the message sender, content, time sent,
 * associated chatroom, and optional file information.
 */
@Data
public class ReportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String reporterUsername;
    private String reason;
}