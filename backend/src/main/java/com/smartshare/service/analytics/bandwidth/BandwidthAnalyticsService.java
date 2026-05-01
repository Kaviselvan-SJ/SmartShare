package com.smartshare.service.analytics.bandwidth;

import com.smartshare.exception.analytics.bandwidth.BandwidthAnalyticsException;
import com.smartshare.model.dto.analytics.BandwidthSavingsDTO;
import com.smartshare.model.dto.analytics.SystemBandwidthSavingsDTO;
import com.smartshare.model.dto.analytics.UserBandwidthSavingsDTO;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BandwidthAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(BandwidthAnalyticsService.class);

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public BandwidthSavingsDTO calculateFileSavings(UUID fileId) {
        try {
            FileEntity file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new BandwidthAnalyticsException("File not found"));

            long originalSize = file.getOriginalSize();
            long compressedSize = file.getCompressedSize();
            long savedBytes = originalSize - compressedSize;
            double compressionRatio = calculateRatio(originalSize, compressedSize);

            logger.info("Calculated bandwidth savings for file {}: {} bytes saved", fileId, savedBytes);

            return BandwidthSavingsDTO.builder()
                    .fileId(file.getId())
                    .originalSize(originalSize)
                    .compressedSize(compressedSize)
                    .savedBytes(savedBytes)
                    .compressionRatio(compressionRatio)
                    .build();
        } catch (BandwidthAnalyticsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to calculate file savings", e);
            throw new BandwidthAnalyticsException("Aggregation failure", e);
        }
    }

    public UserBandwidthSavingsDTO calculateUserSavings(String firebaseUid) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByFirebaseUid(firebaseUid);
            
            if (userOpt.isEmpty()) {
                logger.info("User {} not found, returning 0 bandwidth savings", firebaseUid);
                return UserBandwidthSavingsDTO.builder()
                        .firebaseUid(firebaseUid)
                        .totalOriginalSize(0)
                        .totalCompressedSize(0)
                        .totalSavedBytes(0)
                        .averageCompressionRatio(0.0)
                        .fileCount(0)
                        .build();
            }

            UserEntity user = userOpt.get();
            List<FileEntity> userFiles = fileRepository.findByOwnerOrderByCreatedAtDesc(user);

            long totalOriginal = 0;
            long totalCompressed = 0;

            for (FileEntity file : userFiles) {
                totalOriginal += file.getOriginalSize();
                totalCompressed += file.getCompressedSize();
            }

            long totalSavedBytes = totalOriginal - totalCompressed;
            double averageCompressionRatio = calculateRatio(totalOriginal, totalCompressed);

            logger.info("Calculated user savings for user {}: {} bytes saved", firebaseUid, totalSavedBytes);

            return UserBandwidthSavingsDTO.builder()
                    .firebaseUid(firebaseUid)
                    .totalOriginalSize(totalOriginal)
                    .totalCompressedSize(totalCompressed)
                    .totalSavedBytes(totalSavedBytes)
                    .averageCompressionRatio(averageCompressionRatio)
                    .fileCount(userFiles.size())
                    .build();
        } catch (BandwidthAnalyticsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to calculate user savings", e);
            throw new BandwidthAnalyticsException("Aggregation failure", e);
        }
    }

    public SystemBandwidthSavingsDTO calculateTotalSystemSavings() {
        try {
            List<FileEntity> allFiles = fileRepository.findAll();

            long totalOriginal = 0;
            long totalCompressed = 0;

            for (FileEntity file : allFiles) {
                totalOriginal += file.getOriginalSize();
                totalCompressed += file.getCompressedSize();
            }

            long totalSavedBytes = totalOriginal - totalCompressed;
            double averageCompressionRatio = calculateRatio(totalOriginal, totalCompressed);

            logger.info("Calculated system savings: {} bytes saved globally", totalSavedBytes);

            return SystemBandwidthSavingsDTO.builder()
                    .totalOriginalSize(totalOriginal)
                    .totalCompressedSize(totalCompressed)
                    .totalSavedBytes(totalSavedBytes)
                    .averageCompressionRatio(averageCompressionRatio)
                    .totalFilesProcessed(allFiles.size())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to calculate system savings", e);
            throw new BandwidthAnalyticsException("Aggregation failure", e);
        }
    }

    private double calculateRatio(long original, long compressed) {
        if (original == 0) return 0.0;
        return (double) (original - compressed) / original;
    }
}
