package com.smartshare.service.file;

import com.smartshare.exception.file.FileDeletionException;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.ShortLinkEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.FileGroupRepository;
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
    private final FileGroupRepository fileGroupRepository;
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

            // 1. Unlink from FileGroup
            com.smartshare.model.entity.FileGroupEntity group = fileEntity.getFileGroup();
            if (group != null) {
                if (group.getFiles().size() <= 1) {
                    // This is the last file in the group, delete the group
                    fileGroupRepository.delete(group);
                    fileRepository.flush();
                } else {
                    group.getFiles().remove(fileEntity);
                    if (group.getCurrentVersionId() != null && group.getCurrentVersionId().equals(fileId)) {
                        // We are deleting the active version, switch active version to another file
                        FileEntity newActive = group.getFiles().stream()
                                .max((f1, f2) -> f1.getVersionNumber().compareTo(f2.getVersionNumber()))
                                .orElseThrow(() -> new FileDeletionException("Failed to find fallback version"));
                        
                        newActive.setIsCurrentVersion(true);
                        newActive.setReplacedAt(null);
                        fileRepository.save(newActive);
                        
                        group.setCurrentVersionId(newActive.getId());
                    }
                    fileGroupRepository.save(group);
                }
            }

            // 2. Invalidate Redis Cache & Delete Short Links
            for (ShortLinkEntity link : fileEntity.getShortLinks()) {
                redisCacheService.deleteStoragePath(link.getShortCode());
            }
            shortLinkRepository.deleteByFile_Id(fileId);

            // 3. Check if we should delete physical file and metadata (Deduplication Check)
            boolean isLastHashInstance = fileRepository.countByFileHash(fileHash) <= 1;

            // 4. Delete FileEntity
            fileRepository.delete(fileEntity);
            fileRepository.flush();

            if (isLastHashInstance) {
                logger.info("Hash {} has no other references. Deleting from MinIO and metadata.", fileHash);
                tagRepository.deleteByFileHash(fileHash);
                downloadAnalyticsRepository.deleteByFileHash(fileHash);
                if (minioStorageService.objectExists(fileHash)) {
                    minioStorageService.deleteFile(fileHash);
                }
            } else {
                logger.info("Hash {} is still referenced by other files. Keeping MinIO and metadata.", fileHash);
            }

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
