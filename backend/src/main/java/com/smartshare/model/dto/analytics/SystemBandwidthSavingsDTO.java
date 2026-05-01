package com.smartshare.model.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemBandwidthSavingsDTO {
    private long totalOriginalSize;
    private long totalCompressedSize;
    private long totalSavedBytes;
    private double averageCompressionRatio;
    private int totalFilesProcessed;
}
