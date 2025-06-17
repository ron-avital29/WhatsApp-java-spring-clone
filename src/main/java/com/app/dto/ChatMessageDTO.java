package com.app.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String from;
    private String text;
    private String time;         // still used for frontend display
    private Long chatroomId;     // required for saving to DB
    private Long fromId;

    // Optional file metadata (sent only if a file is attached)
    private Long fileId;
    private String filename;
    private String mimeType;
}
