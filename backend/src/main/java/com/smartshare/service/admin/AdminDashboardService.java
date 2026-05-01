package com.smartshare.service.admin;

import com.smartshare.model.dto.admin.*;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.analytics.DownloadAnalyticsEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.UserRepository;
import com.smartshare.repository.analytics.DownloadAnalyticsRepository;
import com.smartshare.service.analytics.bandwidth.BandwidthAnalyticsService;
import com.smartshare.service.tagging.search.TagSearchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final DownloadAnalyticsRepository downloadAnalyticsRepository;
    private final BandwidthAnalyticsService bandwidthAnalyticsService;
    private final TagSearchService tagSearchService;

    public AdminOverviewDTO getAdminOverview() {
        long totalUsers = userRepository.count();
        long totalFiles = fileRepository.count();
        long totalUploads = totalFiles; // Uploads correlate to files in this simple model
        long totalDownloads = downloadAnalyticsRepository.count();

        var bandwidthStats = bandwidthAnalyticsService.calculateTotalSystemSavings();
        long activeUploaders = fileRepository.countActiveUsersLast24Hours();
        long activeDownloadIPs = downloadAnalyticsRepository.countActiveIPsLast24Hours();

        // Approximate active users
        long activeUsersLast24Hours = activeUploaders + activeDownloadIPs;

        return AdminOverviewDTO.builder()
                .totalUsers(totalUsers)
                .totalFiles(totalFiles)
                .totalUploads(totalUploads)
                .totalDownloads(totalDownloads)
                .totalBandwidthSaved(bandwidthStats.getTotalSavedBytes())
                .averageCompressionRatio(bandwidthStats.getAverageCompressionRatio())
                .activeUsersLast24Hours(activeUsersLast24Hours)
                .build();
    }

    public AdminUsageStatsDTO getUsageTrends() {
        return AdminUsageStatsDTO.builder()
                .uploadsPerDay(processTrendData(fileRepository.findUploadsPerDayLast7Days()))
                .downloadsPerDay(processTrendData(downloadAnalyticsRepository.findDownloadsPerDayLast7Days()))
                .newUsersPerDay(processTrendData(userRepository.findNewUsersPerDayLast7Days()))
                .build();
    }

    private Map<String, Long> processTrendData(List<Object[]> rawData) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rawData) {
            Date date = (Date) row[0];
            Long count = ((Number) row[1]).longValue();
            map.put(date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE), count);
        }
        return map;
    }

    public List<AdminTagStatsDTO> getPopularTags() {
        return tagSearchService.getPopularTags().stream()
                .map(dto -> AdminTagStatsDTO.builder()
                        .tag(dto.getTag())
                        .usageCount(dto.getUsageCount())
                        .build())
                .limit(20)
                .collect(Collectors.toList());
    }

    public AdminStorageStatsDTO getStorageStats() {
        var bandwidthStats = bandwidthAnalyticsService.calculateTotalSystemSavings();
        return AdminStorageStatsDTO.builder()
                .totalOriginalStorage(bandwidthStats.getTotalOriginalSize())
                .totalCompressedStorage(bandwidthStats.getTotalCompressedSize())
                .totalSavedStorage(bandwidthStats.getTotalSavedBytes())
                .averageCompressionRatio(bandwidthStats.getAverageCompressionRatio())
                .build();
    }

    public List<AdminActivityDTO> getRecentSystemActivity() {
        List<AdminActivityDTO> activityList = new ArrayList<>();

        // Get recent downloads
        List<DownloadAnalyticsEntity> downloads = downloadAnalyticsRepository.findRecentActivity(PageRequest.of(0, 10));
        for (DownloadAnalyticsEntity d : downloads) {
            activityList.add(AdminActivityDTO.builder()
                    .timestamp(d.getCreatedAt())
                    .eventType("DOWNLOAD")
                    .shortCode(d.getShortCode())
                    .fileHashMasked(maskFileHash(d.getFileHash()))
                    .deviceType(d.getDeviceType())
                    .build());
        }

        // Get recent uploads
        List<FileEntity> uploads = fileRepository.findRecentUploads(PageRequest.of(0, 10));
        for (FileEntity f : uploads) {
            activityList.add(AdminActivityDTO.builder()
                    .timestamp(f.getCreatedAt())
                    .eventType("UPLOAD")
                    .shortCode("N/A")
                    .fileHashMasked(maskFileHash(f.getFileHash()))
                    .deviceType("N/A")
                    .build());
        }

        activityList.sort(Comparator.comparing(AdminActivityDTO::getTimestamp).reversed());
        return activityList.stream().limit(10).collect(Collectors.toList());
    }

    private String maskFileHash(String fileHash) {
        if (fileHash == null || fileHash.length() < 16) {
            return "******";
        }
        return fileHash.substring(0, 6) + "******" + fileHash.substring(fileHash.length() - 4);
    }
}
