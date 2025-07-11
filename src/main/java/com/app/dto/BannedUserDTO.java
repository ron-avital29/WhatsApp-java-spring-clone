package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class BannedUserDTO {
    private Long id;
    private String username;
    private LocalDateTime bannedUntil;

    public String getFormattedBannedUntil() {
        if (bannedUntil == null) return "Forever";
        return bannedUntil.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
