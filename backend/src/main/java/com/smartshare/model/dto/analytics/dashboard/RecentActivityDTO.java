package com.smartshare.model.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private String shortCode;
    private String fileHash;
    private LocalDateTime timestamp;
    private String deviceType;
    private String browser;
}
