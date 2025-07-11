package com.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class MessageReportDTO {
    private Long id;
    private String content;
    private String senderUsername;
    private String bannedUntil; // Optional: ISO-8601 formatted string
    private List<ReportDTO> reports;
}