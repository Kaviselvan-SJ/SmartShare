package com.smartshare.service.upload;

import com.smartshare.exception.upload.FileUploadException;
import com.smartshare.model.dto.upload.UploadResponseDTO;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.UserEntity;
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
    private final DeduplicationService deduplicationService;
    private final CompressionService compressionService;
    private final StorageService storageService;
    private final com.smartshare.service.tagging.TaggingService taggingService;

    @Transactional
    public UploadResponseDTO uploadFile(MultipartFile file) {
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
                
                // Fetch the existing file metadata reference
                FileEntity existingFile = fileRepository.findById(dedupResult.getExistingFileId())
                        .orElseThrow(() -> new FileUploadException("Duplicate detected but metadata not found"));
                
                return buildResponse(existingFile, true, "Duplicate detected, linked to existing file");
            }

            logger.info("duplicate: false");

            // Step 4: Compress file if new
            CompressionResult compressionResult;
            try (InputStream is = file.getInputStream()) {
                compressionResult = compressionService.compressFile(is, file.getOriginalFilename());
            }

            // Step 5: Store compressed file in MinIO using the fileHash as object name
            String storagePath = dedupResult.getFileHash();
            try (InputStream compressedStream = compressionResult.getCompressedStream()) {
                storageService.uploadFile(
                        storagePath,
                        compressedStream,
                        compressionResult.getCompressedSize(),
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                );
            }

            logger.info("compressed from {} to {}", formatSize(compressionResult.getOriginalSize()), formatSize(compressionResult.getCompressedSize()));
            logger.info("stored successfully in MinIO");

            // Step 6: Save metadata in PostgreSQL
            FileEntity newFileEntity = FileEntity.builder()
                    .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown")
                    .fileHash(dedupResult.getFileHash())
                    .originalSize(compressionResult.getOriginalSize())
                    .compressedSize(compressionResult.getCompressedSize())
                    .storagePath(storagePath)
                    .owner(user)
                    .build();

            newFileEntity = fileRepository.save(newFileEntity);

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
        java.util.Optional<UserEntity> userOpt = userRepository.findByFirebaseUid(authenticatedUser.getUid());
        
        if (userOpt.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return fileRepository.findByOwnerOrderByCreatedAtDesc(userOpt.get()).stream()
                .map(fileEntity -> buildResponse(fileEntity, false, "Success"))
                .collect(java.util.stream.Collectors.toList());
    }
}
