package com.smartshare.service.file;

import com.smartshare.exception.file.FileDeletionException; // reuse or create new exception
import com.smartshare.model.dto.file.details.FileDetailsDTO;
import com.smartshare.model.dto.file.details.RecentDownloadDTO;
import com.smartshare.model.dto.file.details.ShortLinkAnalyticsDTO;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.ShortLinkEntity;
import com.smartshare.model.entity.tag.TagEntity;
import com.smartshare.model.entity.analytics.DownloadAnalyticsEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.analytics.DownloadAnalyticsRepository;
import com.smartshare.repository.tag.TagRepository;
import com.smartshare.service.analytics.bandwidth.BandwidthAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileDetailsService {

    private final FileRepository fileRepository;
    private final TagRepository tagRepository;
    private final DownloadAnalyticsRepository downloadAnalyticsRepository;
    private final BandwidthAnalyticsService bandwidthAnalyticsService;

    @Transactional(readOnly = true)
    public FileDetailsDTO getFileDetails(UUID fileId, String firebaseUid) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getFirebaseUid().equals(firebaseUid)) {
            throw new RuntimeException("Unauthorized: You do not own this file");
        }

        String fileHash = file.getFileHash();

        List<TagEntity> tags = tagRepository.findByFileHash(fileHash);
        List<String> tagStrings = tags.stream().map(TagEntity::getTag).collect(Collectors.toList());

        // Let's use BandwidthAnalyticsService to get file savings
        var savings = bandwidthAnalyticsService.calculateFileSavings(fileId);

        List<ShortLinkAnalyticsDTO> links = file.getShortLinks().stream().map(link -> {
            String status = determineStatus(link);
            return ShortLinkAnalyticsDTO.builder()
                    .shortCode(link.getShortCode())
                    .expiryTime(link.getExpiryTime())
                    .downloadLimit(link.getDownloadLimit())
                    .downloadCount(link.getDownloadCount() != null ? link.getDownloadCount().longValue() : 0L)
                    .passwordProtected(link.getPassword() != null)
                    .password(link.getPassword()) // The frontend can reveal this
                    .status(status)
                    .createdAt(link.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        long activeLinks = links.stream().filter(l -> "ACTIVE".equals(l.getStatus())).count();
        long expiredLinks = links.stream().filter(l -> !"ACTIVE".equals(l.getStatus())).count();

        List<DownloadAnalyticsEntity> recentActivityEntities = downloadAnalyticsRepository.findRecentActivityByFileHash(fileHash, PageRequest.of(0, 10));
        
        List<RecentDownloadDTO> recentActivity = recentActivityEntities.stream().map(a -> 
            RecentDownloadDTO.builder()
                .timestamp(a.getCreatedAt())
                .deviceType(a.getDeviceType())
                .browser(a.getBrowser())
                .shortCode(a.getShortCode())
                .build()
        ).collect(Collectors.toList());

        LocalDateTime lastAccessed = recentActivityEntities.isEmpty() ? null : recentActivityEntities.get(0).getCreatedAt();

        return FileDetailsDTO.builder()
                .fileId(file.getId())
                .fileName(file.getFileName())
                .fileHash(file.getFileHash())
                .originalSize(file.getOriginalSize())
                .compressedSize(file.getCompressedSize())
                .compressionRatio(savings.getCompressionRatio())
                .totalBandwidthSaved(savings.getSavedBytes())
                .totalDownloads(links.stream().mapToLong(ShortLinkAnalyticsDTO::getDownloadCount).sum())
                .totalLinks(links.size())
                .activeLinkCount(activeLinks)
                .expiredLinkCount(expiredLinks)
                .lastAccessedAt(lastAccessed)
                .tags(tagStrings)
                .links(links)
                .recentActivity(recentActivity)
                .build();
    }

    private String determineStatus(ShortLinkEntity link) {
        if (link.getExpiryTime() != null && link.getExpiryTime().isBefore(LocalDateTime.now())) {
            return "EXPIRED";
        }
        if (link.getDownloadLimit() != null && link.getDownloadCount() != null && link.getDownloadCount() >= link.getDownloadLimit()) {
            return "LIMIT_REACHED";
        }
        return "ACTIVE";
    }
}
