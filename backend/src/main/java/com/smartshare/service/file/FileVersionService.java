package com.smartshare.service.file;

import com.smartshare.exception.file.PreviewAccessException;
import com.smartshare.exception.upload.FileUploadException;
import com.smartshare.model.dto.file.FileVersionDTO;
import com.smartshare.model.dto.upload.UploadResponseDTO;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.FileGroupEntity;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.repository.FileGroupRepository;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.UserRepository;
import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.compression.CompressionResult;
import com.smartshare.service.compression.CompressionService;
import com.smartshare.service.deduplication.DeduplicationResult;
import com.smartshare.service.deduplication.DeduplicationService;
import com.smartshare.service.storage.StorageService;
import com.smartshare.service.tagging.TaggingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileVersionService {

    private static final Logger logger = LoggerFactory.getLogger(FileVersionService.class);

    private final FileGroupRepository fileGroupRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final DeduplicationService deduplicationService;
    private final CompressionService compressionService;
    private final StorageService storageService;
    private final TaggingService taggingService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public UploadResponseDTO checkDuplicate(String fileName, AuthenticatedUser authUser) {
        UserEntity user = userRepository.findByFirebaseUid(authUser.getUid())
                .orElseThrow(() -> new FileUploadException("User not found"));

        Optional<FileGroupEntity> groupOpt = fileGroupRepository.findFirstByOwnerAndDisplayFileNameOrderByCreatedAtDesc(user, fileName);
        
        if (groupOpt.isPresent()) {
            FileGroupEntity group = groupOpt.get();
            FileEntity currentVersion = null;
            if (group.getCurrentVersionId() != null) {
                currentVersion = fileRepository.findById(group.getCurrentVersionId()).orElse(null);
            }
            
            // If the group has no current version and no files, it's an orphaned ghost group. Let's clean it up!
            if (currentVersion == null && group.getFiles().isEmpty()) {
                logger.info("Found orphaned file group {}, deleting it.", group.getId());
                fileGroupRepository.delete(group);
                // Since we deleted the ghost, there is no real conflict.
                return UploadResponseDTO.builder()
                        .fileNameExists(false)
                        .message("Filename is unique (cleaned up ghost)")
                        .build();
            }
            
            return UploadResponseDTO.builder()
                    .fileNameExists(true)
                    .existingFileGroupId(group.getId())
                    .existingCurrentVersion(currentVersion != null ? currentVersion.getVersionNumber() : 1)
                    .previewAvailable(true)
                    .message("Filename conflict detected")
                    .build();
        }

        return UploadResponseDTO.builder()
                .fileNameExists(false)
                .message("Filename is unique")
                .build();
    }

    @Transactional
    public UploadResponseDTO addVersion(UUID fileGroupId, MultipartFile file, boolean replaceCurrent) {
        AuthenticatedUser authUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = userRepository.findByFirebaseUid(authUser.getUid())
                .orElseThrow(() -> new FileUploadException("User not found"));

        FileGroupEntity group = fileGroupRepository.findById(fileGroupId)
                .orElseThrow(() -> new FileUploadException("File group not found"));

        if (!group.getOwner().getId().equals(user.getId())) {
            throw new PreviewAccessException("Unauthorized to modify this file group");
        }

        try {
            // Deduplication Check
            DeduplicationResult dedupResult;
            try (InputStream is = file.getInputStream()) {
                dedupResult = deduplicationService.checkDuplicate(is);
            }

            String mimeType = file.getContentType();
            if (mimeType == null || mimeType.isBlank()) {
                try {
                    mimeType = java.nio.file.Files.probeContentType(java.nio.file.Paths.get(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"));
                } catch (Exception ignored) {}
            }
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "application/octet-stream";
            }

            long originalSize, compressedSize;
            String storagePath = dedupResult.getFileHash();

            if (!dedupResult.isDuplicateFound()) {
                CompressionResult compressionResult;
                try (InputStream is = file.getInputStream()) {
                    compressionResult = compressionService.compressFile(is, file.getOriginalFilename());
                }
                try (InputStream compressedStream = compressionResult.getCompressedStream()) {
                    storageService.uploadFile(storagePath, compressedStream, compressionResult.getCompressedSize(), mimeType);
                }
                originalSize = compressionResult.getOriginalSize();
                compressedSize = compressionResult.getCompressedSize();
            } else {
                FileEntity existingFile = fileRepository.findById(dedupResult.getExistingFileId())
                        .orElseThrow(() -> new FileUploadException("Duplicate metadata not found"));
                originalSize = existingFile.getOriginalSize();
                compressedSize = existingFile.getCompressedSize();
            }

            // Version Logic
            int nextVersion = group.getFiles().stream().mapToInt(FileEntity::getVersionNumber).max().orElse(0) + 1;

            group.getFiles().forEach(f -> {
                if (Boolean.TRUE.equals(f.getIsCurrentVersion())) {
                    f.setIsCurrentVersion(false);
                    if (replaceCurrent) {
                        f.setReplacedAt(LocalDateTime.now());
                    }
                    fileRepository.save(f);
                }
            });

            FileEntity newVersion = FileEntity.builder()
                    .fileName(group.getDisplayFileName())
                    .fileHash(dedupResult.getFileHash())
                    .originalSize(originalSize)
                    .compressedSize(compressedSize)
                    .storagePath(storagePath)
                    .mimeType(mimeType)
                    .owner(user)
                    .fileGroup(group)
                    .versionNumber(nextVersion)
                    .isCurrentVersion(true)
                    .build();

            newVersion = fileRepository.save(newVersion);
            group.setCurrentVersionId(newVersion.getId());
            fileGroupRepository.save(group);

            // Regenerate tags for the active version
            taggingService.generateTags(newVersion.getFileName(), newVersion.getFileHash());
            
            // Invalidate redis caches for all short links associated with this group
            invalidateShortLinks(group);

            return UploadResponseDTO.builder()
                    .fileId(newVersion.getId())
                    .fileName(newVersion.getFileName())
                    .fileHash(newVersion.getFileHash())
                    .originalSize(newVersion.getOriginalSize())
                    .compressedSize(newVersion.getCompressedSize())
                    .duplicate(dedupResult.isDuplicateFound())
                    .message("Version uploaded successfully")
                    .build();

        } catch (Exception e) {
            logger.error("Version upload failed", e);
            throw new FileUploadException("Failed to upload new version: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void switchCurrentVersion(UUID fileGroupId, UUID versionId, AuthenticatedUser authUser) {
        UserEntity user = userRepository.findByFirebaseUid(authUser.getUid())
                .orElseThrow(() -> new FileUploadException("User not found"));

        FileGroupEntity group = fileGroupRepository.findById(fileGroupId)
                .orElseThrow(() -> new FileUploadException("File group not found"));

        if (!group.getOwner().getId().equals(user.getId())) {
            throw new PreviewAccessException("Unauthorized to modify this file group");
        }

        FileEntity newCurrent = fileRepository.findById(versionId)
                .orElseThrow(() -> new FileUploadException("Version not found"));

        if (!newCurrent.getFileGroup().getId().equals(group.getId())) {
            throw new FileUploadException("Version does not belong to this group");
        }

        FileEntity oldCurrent = fileRepository.findById(group.getCurrentVersionId()).orElse(null);
        if (oldCurrent != null) {
            oldCurrent.setIsCurrentVersion(false);
            fileRepository.save(oldCurrent);
        }

        newCurrent.setIsCurrentVersion(true);
        newCurrent.setReplacedAt(null);
        fileRepository.save(newCurrent);

        group.setCurrentVersionId(newCurrent.getId());
        fileGroupRepository.save(group);

        taggingService.generateTags(newCurrent.getFileName(), newCurrent.getFileHash());
        invalidateShortLinks(group);
        logger.info("Switched active version of group {} to version {}", fileGroupId, versionId);
    }

    @Transactional(readOnly = true)
    public List<FileVersionDTO> getVersionHistory(UUID fileGroupId, AuthenticatedUser authUser) {
        FileGroupEntity group = fileGroupRepository.findById(fileGroupId)
                .orElseThrow(() -> new FileUploadException("File group not found"));

        if (!group.getOwner().getFirebaseUid().equals(authUser.getUid())) {
            throw new PreviewAccessException("Unauthorized");
        }

        return group.getFiles().stream()
                .sorted((f1, f2) -> f2.getVersionNumber().compareTo(f1.getVersionNumber()))
                .map(file -> FileVersionDTO.builder()
                        .versionId(file.getId())
                        .versionNumber(file.getVersionNumber())
                        .uploadedAt(file.getCreatedAt())
                        .isCurrentVersion(file.getIsCurrentVersion())
                        .originalSize(file.getOriginalSize())
                        .compressedSize(file.getCompressedSize())
                        .build())
                .collect(Collectors.toList());
    }

    private void invalidateShortLinks(FileGroupEntity group) {
        // Iterate through all versions and their short links
        group.getFiles().forEach(file -> {
            file.getShortLinks().forEach(link -> {
                redisTemplate.delete("shortlink:" + link.getShortCode());
            });
        });
    }
}
