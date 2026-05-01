package com.smartshare.model.dto.file.details;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDetailsDTO {
    private UUID fileId;
    private String fileName;
    private String fileHash;
    private long originalSize;
    private long compressedSize;
    private double compressionRatio;
    private long totalBandwidthSaved;
    private long totalDownloads;
    private long totalLinks;
    private long activeLinkCount;
    private long expiredLinkCount;
    private LocalDateTime lastAccessedAt;
    private List<String> tags;
    private List<ShortLinkAnalyticsDTO> links;
    private List<RecentDownloadDTO> recentActivity;
}
