package com.smartshare.model.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminActivityDTO {
    private LocalDateTime timestamp;
    private String eventType;
    private String shortCode;
    private String fileHashMasked;
    private String deviceType;
}
