package com.smartshare.model.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOverviewDTO {
    private long totalUsers;
    private long totalFiles;
    private long totalUploads;
    private long totalDownloads;
    private long totalBandwidthSaved;
    private double averageCompressionRatio;
    private long activeUsersLast24Hours;
}
