package com.smartshare.model.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewDTO {
    private long totalFiles;
    private long totalDownloads;
    private long totalBandwidthSaved;
    private double averageCompressionRatio;
}
