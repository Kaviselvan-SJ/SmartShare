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
public class ShortLinkAnalyticsDTO {
    private String shortCode;
    private LocalDateTime expiryTime;
    private Integer downloadLimit;
    private Long downloadCount;
    private boolean passwordProtected;
    private String password; // Will only be populated on demand for the owner
    private String status; // ACTIVE, EXPIRED, LIMIT_REACHED
    private LocalDateTime createdAt;
}
