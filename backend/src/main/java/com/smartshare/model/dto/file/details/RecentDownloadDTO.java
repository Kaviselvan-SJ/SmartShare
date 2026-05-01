package com.smartshare.model.dto.file.details;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentDownloadDTO {
    private LocalDateTime timestamp;
    private String deviceType;
    private String browser;
    private String shortCode;
}
