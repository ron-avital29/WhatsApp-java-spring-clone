package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String type; // "JOIN" or "LEAVE"
}
