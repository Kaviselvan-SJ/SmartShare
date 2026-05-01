package com.smartshare.service.analytics.dashboard;

import com.smartshare.exception.analytics.dashboard.DashboardAnalyticsException;
import com.smartshare.model.dto.analytics.dashboard.*;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.model.entity.analytics.DownloadAnalyticsEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.ShortLinkRepository;
import com.smartshare.repository.UserRepository;
import com.smartshare.repository.analytics.DownloadAnalyticsRepository;
import com.smartshare.repository.tag.TagRepository;
import com.smartshare.service.analytics.bandwidth.BandwidthAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardAnalyticsService.class);

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final DownloadAnalyticsRepository downloadAnalyticsRepository;
    private final TagRepository tagRepository;
    private final BandwidthAnalyticsService bandwidthAnalyticsService;
    private final ShortLinkRepository shortLinkRepository;

    @Transactional(readOnly = true)
    public SystemOverviewDTO getSystemOverview() {
        try {
            String firebaseUid = getAuthenticatedUid();

            // Safe: countByOwner_FirebaseUid returns 0 if user has no files
            long totalFiles = fileRepository.countByOwner_FirebaseUid(firebaseUid);
            long totalDownloads = downloadAnalyticsRepository.countByUserFiles(firebaseUid);

            var bandwidthSavings = bandwidthAnalyticsService.calculateUserSavings(firebaseUid);

            return SystemOverviewDTO.builder()
                    .totalFiles(totalFiles)
                    .totalDownloads(totalDownloads)
                    .totalBandwidthSaved(bandwidthSavings.getTotalSavedBytes())
                    .averageCompressionRatio(bandwidthSavings.getAverageCompressionRatio())
                    .build();
        } catch (DashboardAnalyticsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to calculate user overview", e);
            // Return safe zero-state for brand new users instead of 500
            return SystemOverviewDTO.builder()
                    .totalFiles(0)
                    .totalDownloads(0)
                    .totalBandwidthSaved(0)
                    .averageCompressionRatio(0.0)
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public List<TopFilesDTO> getTopDownloadedFiles() {
        try {
            String firebaseUid = getAuthenticatedUid();
            List<Object[]> topFiles = downloadAnalyticsRepository.findTopDownloadedFilesByUser(firebaseUid, PageRequest.of(0, 10));

            return topFiles.stream().map(row -> {
                String fileHash = (String) row[0];
                long count = (Long) row[1];
                
                Optional<FileEntity> fileOpt = fileRepository.findByFileHash(fileHash);
                
                return TopFilesDTO.builder()
                        .fileId(fileOpt.map(FileEntity::getId).orElse(null))
                        .fileName(fileOpt.map(FileEntity::getFileName).orElse("Unknown"))
                        .fileHash(fileHash)
                        .downloadCount(count)
                        .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Could not load top files (possibly new user): {}", e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<PopularTagsDTO> getPopularTags() {
        try {
            String firebaseUid = getAuthenticatedUid();
            return tagRepository.findPopularTagsByUser(firebaseUid, PageRequest.of(0, 10)).stream()
                    .map(row -> new PopularTagsDTO((String) row[0], (Long) row[1]))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Could not load popular tags (possibly new user): {}", e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<RecentActivityDTO> getRecentActivity() {
        try {
            String firebaseUid = getAuthenticatedUid();
            List<DownloadAnalyticsEntity> recent = downloadAnalyticsRepository.findRecentActivityByUser(firebaseUid, PageRequest.of(0, 20));

            return recent.stream().map(activity -> RecentActivityDTO.builder()
                    .shortCode(activity.getShortCode())
                    .fileHash(activity.getFileHash())
                    .timestamp(activity.getCreatedAt())
                    .deviceType(activity.getDeviceType())
                    .browser(activity.getBrowser())
                    .build()
            ).collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Could not load recent activity (possibly new user): {}", e.getMessage());
            return List.of();
        }
    }

    private String getAuthenticatedUid() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.smartshare.security.firebase.AuthenticatedUser) {
            return ((com.smartshare.security.firebase.AuthenticatedUser) principal).getUid();
        }
        throw new DashboardAnalyticsException("User not authenticated");
    }
}
