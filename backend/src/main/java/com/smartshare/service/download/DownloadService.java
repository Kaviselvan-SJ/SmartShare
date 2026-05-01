package com.smartshare.service.download;

import com.smartshare.exception.download.DownloadException;
import com.smartshare.model.dto.download.DownloadValidationResult;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.ShortLinkEntity;
import com.smartshare.repository.ShortLinkRepository;
import com.smartshare.service.cache.ShortLinkCacheResolver;
import com.smartshare.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    private final ShortLinkCacheResolver cacheResolver;
    private final ShortLinkRepository shortLinkRepository;
    private final StorageService storageService;

    @Transactional
    public InputStream processDownload(String shortCode, String password) {
        logger.info("ShortCode {} accessed", shortCode);

        // Step 1: Resolve storage path (using cache resolver first)
        String storagePath = cacheResolver.resolveStoragePath(shortCode);

        // Step 2: Retrieve ShortLinkEntity to validate rules and update counts
        ShortLinkEntity shortLink = shortLinkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new DownloadException("Short link not found"));

        // Step 3-5: Validate
        DownloadValidationResult validation = validate(shortLink, password);
        if (!validation.isValid()) {
            logger.warn("Validation failed for shortCode {}: {}", shortCode, validation.getMessage());
            throw new DownloadException(validation.getMessage());
        }

        // Step 6: File metadata is implicitly ready via shortLink.getFile()

        // Step 7: Download file from MinIO
        InputStream fileStream;
        try {
            fileStream = storageService.downloadFile(storagePath);
            logger.info("File stream retrieved successfully from MinIO");
        } catch (Exception e) {
            logger.error("Failed to retrieve file from MinIO", e);
            throw new DownloadException("Storage failure: Could not retrieve file");
        }

        // Step 8: Increment download count
        shortLink.setDownloadCount(shortLink.getDownloadCount() + 1);
        shortLinkRepository.save(shortLink);
        logger.info("Download count updated to {}", shortLink.getDownloadCount());
        
        // Step 9: Return stream
        return fileStream;
    }

    public FileEntity getFileMetadata(String shortCode) {
        return shortLinkRepository.findByShortCode(shortCode)
                .map(ShortLinkEntity::getFile)
                .orElseThrow(() -> new DownloadException("Short link not found"));
    }

    private DownloadValidationResult validate(ShortLinkEntity shortLink, String providedPassword) {
        // Step 3: Validate expiry
        if (shortLink.getExpiryTime() != null && shortLink.getExpiryTime().isBefore(LocalDateTime.now())) {
            return DownloadValidationResult.builder().valid(false).message("Expired link").build();
        }

        // Step 4: Validate download limit
        if (shortLink.getDownloadLimit() != null && shortLink.getDownloadCount() >= shortLink.getDownloadLimit()) {
            return DownloadValidationResult.builder().valid(false).message("Download limit exceeded").build();
        }

        // Step 5: Validate password
        if (shortLink.getPassword() != null && !shortLink.getPassword().isEmpty()) {
            if (providedPassword == null || !shortLink.getPassword().equals(providedPassword)) {
                return DownloadValidationResult.builder().valid(false).message("Password mismatch").build();
            }
        }

        return DownloadValidationResult.builder().valid(true).build();
    }
}
