package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputMessageDTO {
    private String from;
    private String text;
    private String time;
}
