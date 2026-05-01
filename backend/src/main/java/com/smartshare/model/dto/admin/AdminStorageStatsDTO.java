package com.smartshare.model.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStorageStatsDTO {
    private long totalOriginalStorage;
    private long totalCompressedStorage;
    private long totalSavedStorage;
    private double averageCompressionRatio;
}
