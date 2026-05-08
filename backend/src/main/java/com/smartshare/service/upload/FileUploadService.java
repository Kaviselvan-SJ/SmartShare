package com.smartshare.service.upload;

import com.smartshare.exception.upload.FileUploadException;
import com.smartshare.model.dto.upload.UploadResponseDTO;
import com.smartshare.model.entity.FileEntity;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileGroupRepository fileGroupRepository;
    private final DeduplicationService deduplicationService;
    private final CompressionService compressionService;
    private final StorageService storageService;
    private final com.smartshare.service.tagging.TaggingService taggingService;

    @Transactional
    public UploadResponseDTO uploadFile(MultipartFile file, boolean independent) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Cannot upload empty file");
        }

        try {
            // Step 1: Extract authenticated user from SecurityContext
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity user = getOrCreateUser(authenticatedUser);

            logger.info("User {} uploaded {}", user.getFirebaseUid(), file.getOriginalFilename());

            // Step 2 & 3: Check duplicate using DeduplicationService
            DeduplicationResult dedupResult;
            try (InputStream is = file.getInputStream()) {
                dedupResult = deduplicationService.checkDuplicate(is);
            }
            
            if (dedupResult.isDuplicateFound()) {
                logger.info("duplicate: true");
                
                if (!independent) {
                    // Fetch the existing file metadata reference
                    FileEntity existingFile = fileRepository.findById(dedupResult.getExistingFileId())
                            .orElseThrow(() -> new FileUploadException("Duplicate detected but metadata not found"));
                    
                    return buildResponse(existingFile, true, "Duplicate detected, linked to existing file");
                } else {
                    logger.info("independent flag is true, bypassing existing file return to create a new independent metadata record");
                }
            }

            logger.info("duplicate: false");

            long finalOriginalSize;
            long finalCompressedSize;
            String storagePath = dedupResult.getFileHash();

            if (!dedupResult.isDuplicateFound()) {
                // Step 4: Compress file if new
                CompressionResult compressionResult;
                try (InputStream is = file.getInputStream()) {
                    compressionResult = compressionService.compressFile(is, file.getOriginalFilename());
                }

                // Detect MIME type
                String mimeType = file.getContentType();
                if (mimeType == null || mimeType.isBlank()) {
                    try {
                        mimeType = java.nio.file.Files.probeContentType(java.nio.file.Paths.get(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"));
                    } catch (Exception e) {
                        logger.warn("Could not probe content type for file: {}", file.getOriginalFilename());
                    }
                }
                if (mimeType == null || mimeType.isBlank()) {
                    mimeType = "application/octet-stream";
                }

                // Step 5: Store compressed file in MinIO using the fileHash as object name
                try (InputStream compressedStream = compressionResult.getCompressedStream()) {
                    storageService.uploadFile(
                            storagePath,
                            compressedStream,
                            compressionResult.getCompressedSize(),
                            mimeType
                    );
                }

                finalOriginalSize = compressionResult.getOriginalSize();
                finalCompressedSize = compressionResult.getCompressedSize();
                logger.info("compressed from {} to {}", formatSize(finalOriginalSize), formatSize(finalCompressedSize));
                logger.info("stored successfully in MinIO");
            } else {
                // Independent file logic, but physical file already exists in MinIO
                FileEntity duplicateSource = fileRepository.findById(dedupResult.getExistingFileId())
                        .orElseThrow(() -> new FileUploadException("Duplicate detected but metadata not found"));
                finalOriginalSize = duplicateSource.getOriginalSize();
                finalCompressedSize = duplicateSource.getCompressedSize();
            }

            // Detect MIME type again if skipped above
            String mimeType = file.getContentType();
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "application/octet-stream";
            }

            // Step 6: Save metadata in PostgreSQL
            String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            
            // Create new FileGroup for this upload
            com.smartshare.model.entity.FileGroupEntity fileGroup = com.smartshare.model.entity.FileGroupEntity.builder()
                    .owner(user)
                    .displayFileName(originalFileName)
                    .build();
            fileGroup = fileGroupRepository.save(fileGroup);

            FileEntity newFileEntity = FileEntity.builder()
                    .fileName(originalFileName)
                    .fileHash(dedupResult.getFileHash())
                    .originalSize(finalOriginalSize)
                    .compressedSize(finalCompressedSize)
                    .storagePath(storagePath)
                    .mimeType(mimeType)
                    .owner(user)
                    .fileGroup(fileGroup)
                    .versionNumber(1)
                    .isCurrentVersion(true)
                    .build();

            newFileEntity = fileRepository.save(newFileEntity);
            
            // Link group to its current version
            fileGroup.setCurrentVersionId(newFileEntity.getId());
            fileGroupRepository.save(fileGroup);

            // Step 6.5: Generate Tags
            taggingService.generateTags(newFileEntity.getFileName(), newFileEntity.getFileHash());

            // Step 7: Return upload response
            return buildResponse(newFileEntity, false, "File uploaded successfully");

        } catch (Exception e) {
            logger.error("Upload failed", e);
            throw new FileUploadException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private UserEntity getOrCreateUser(AuthenticatedUser authUser) {
        return userRepository.findByFirebaseUid(authUser.getUid())
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .firebaseUid(authUser.getUid())
                            .email(authUser.getEmail())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    private UploadResponseDTO buildResponse(FileEntity fileEntity, boolean duplicate, String message) {
        return UploadResponseDTO.builder()
                .fileId(fileEntity.getId())
                .fileName(fileEntity.getFileName())
                .fileHash(fileEntity.getFileHash())
                .originalSize(fileEntity.getOriginalSize())
                .compressedSize(fileEntity.getCompressedSize())
                .duplicate(duplicate)
                .message(message)
                .versionNumber(fileEntity.getVersionNumber())
                .build();
    }
    
    private String formatSize(long size) {
        if (size >= 1024 * 1024) {
            return (size / (1024 * 1024)) + "MB";
        } else if (size >= 1024) {
            return (size / 1024) + "KB";
        }
        return size + "B";
    }

    @Transactional(readOnly = true)
    public java.util.List<UploadResponseDTO> getMyFiles() {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        return userRepository.findByFirebaseUid(authenticatedUser.getUid())
                .map(user -> fileRepository.findByOwnerAndIsCurrentVersionTrueOrderByCreatedAtDesc(user).stream()
                        .map(fileEntity -> buildResponse(fileEntity, false, "Success"))
                        .collect(java.util.stream.Collectors.toList()))
                .orElse(java.util.Collections.emptyList()); // New user: safely return empty list
    }
}
