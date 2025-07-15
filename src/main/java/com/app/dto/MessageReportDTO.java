package com.app.dto;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * MessageReportDTO represents a report on a message in the application.
 * It contains details about the message, the sender, associated reports,
 * and optional file information.
 */
@Data
public class MessageReportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private String senderUsername;
    private String bannedUntil;
    private List<ReportDTO> reports;
    private String lastUpdated;
    private Long fileId;
    private String fileName;
    private String fileMimeType;
}