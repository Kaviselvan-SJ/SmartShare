package com.smartshare.service.file;

import com.smartshare.exception.file.FileDeletionException;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.ShortLinkEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.ShortLinkRepository;
import com.smartshare.repository.analytics.DownloadAnalyticsRepository;
import com.smartshare.repository.tag.TagRepository;
import com.smartshare.service.cache.RedisCacheService;
import com.smartshare.service.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileDeletionService {

    private static final Logger logger = LoggerFactory.getLogger(FileDeletionService.class);

    private final FileRepository fileRepository;
    private final ShortLinkRepository shortLinkRepository;
    private final TagRepository tagRepository;
    private final DownloadAnalyticsRepository downloadAnalyticsRepository;
    private final MinioStorageService minioStorageService;
    private final RedisCacheService redisCacheService;

    @Transactional
    public void deleteFile(UUID fileId, String firebaseUid) {
        try {
            FileEntity fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileDeletionException("File not found"));

            if (!fileEntity.getOwner().getFirebaseUid().equals(firebaseUid)) {
                throw new FileDeletionException("Unauthorized: You do not own this file");
            }

            String fileHash = fileEntity.getFileHash();

            // 1. Invalidate Redis Cache explicitly for all associated short links
            for (ShortLinkEntity link : fileEntity.getShortLinks()) {
                redisCacheService.deleteStoragePath(link.getShortCode());
            }

            // 2. Delete ShortLinkEntity records explicitly
            shortLinkRepository.deleteByFile_Id(fileId);

            // 3. Delete TagEntity records
            tagRepository.deleteByFileHash(fileHash);

            // 4. Delete DownloadAnalyticsEntity records
            downloadAnalyticsRepository.deleteByFileHash(fileHash);

            // 5. Delete MinIO object (using fileHash as requested)
            if (minioStorageService.objectExists(fileHash)) {
                minioStorageService.deleteFile(fileHash);
            }

            // 6. Delete FileEntity
            fileRepository.delete(fileEntity);

            // 7. Audit Logging
            logger.info("AUDIT LOG: [userId={}, fileId={}, timestamp={}, actionType=DELETE_FILE]", 
                    firebaseUid, fileId, Instant.now());

        } catch (FileDeletionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to execute cascade delete for file: " + fileId, e);
            throw new FileDeletionException("Failed to delete file and its associated data", e);
        }
    }
}
