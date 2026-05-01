package com.smartshare.model.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandwidthSavingsDTO {
    private UUID fileId;
    private long originalSize;
    private long compressedSize;
    private long savedBytes;
    private double compressionRatio;
}
