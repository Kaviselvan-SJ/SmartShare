package com.smartshare.service.file.preview;

import com.smartshare.exception.file.PreviewAccessException;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilePreviewService {

    private static final Logger logger = LoggerFactory.getLogger(FilePreviewService.class);

    private final FileRepository fileRepository;
    private final StorageService storageService;

    public static class PreviewData {
        public final InputStream stream;
        public final String mimeType;
        public final String originalFileName;

        public PreviewData(InputStream stream, String mimeType, String originalFileName) {
            this.stream = stream;
            this.mimeType = mimeType;
            this.originalFileName = originalFileName;
        }
    }

    @Transactional(readOnly = true)
    public PreviewData previewFile(UUID fileId, String firebaseUid) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new PreviewAccessException("File not found or access denied"));

        // Validate ownership
        if (!file.getOwner().getFirebaseUid().equals(firebaseUid)) {
            logger.warn("Unauthorized preview attempt for file {} by user {}", fileId, firebaseUid);
            throw new PreviewAccessException("You do not have permission to preview this file");
        }

        logger.info("User {} is previewing file {}", firebaseUid, file.getFileName());

        try {
            InputStream stream = storageService.downloadFile(file.getFileHash());
            
            String mimeType = file.getMimeType();
            if (mimeType == null || mimeType.isBlank()) {
                String fileName = file.getFileName().toLowerCase();
                if (fileName.endsWith(".pdf")) mimeType = "application/pdf";
                else if (fileName.endsWith(".png")) mimeType = "image/png";
                else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) mimeType = "image/jpeg";
                else if (fileName.endsWith(".gif")) mimeType = "image/gif";
                else if (fileName.endsWith(".webp")) mimeType = "image/webp";
                else if (fileName.endsWith(".txt")) mimeType = "text/plain";
                else if (fileName.endsWith(".json")) mimeType = "application/json";
                else {
                    try {
                        mimeType = java.nio.file.Files.probeContentType(java.nio.file.Paths.get(file.getFileName()));
                    } catch (Exception ignored) {}
                }
                
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
            }
            
            return new PreviewData(stream, mimeType, file.getFileName());
        } catch (Exception e) {
            logger.error("Failed to retrieve file stream for preview", e);
            throw new RuntimeException("Could not retrieve file for preview", e);
        }
    }
}
