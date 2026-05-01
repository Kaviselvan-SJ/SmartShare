package com.smartshare.service.deduplication;

import com.smartshare.model.entity.FileEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.util.hash.HashUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeduplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DeduplicationService.class);

    private final FileRepository fileRepository;

    public DeduplicationResult checkDuplicate(InputStream stream) {
        String fileHash = HashUtil.generateSha256(stream);
        logger.info("hash generated: {}", fileHash);

        Optional<FileEntity> existingFile = fileRepository.findByFileHash(fileHash);

        if (existingFile.isPresent()) {
            logger.info("duplicate found: true");
            return DeduplicationResult.builder()
                    .duplicateFound(true)
                    .fileHash(fileHash)
                    .existingStoragePath(existingFile.get().getStoragePath())
                    .existingFileId(existingFile.get().getId())
                    .build();
        } else {
            logger.info("duplicate found: false");
            return DeduplicationResult.builder()
                    .duplicateFound(false)
                    .fileHash(fileHash)
                    .build();
        }
    }

    public boolean fileExists(String hash) {
        return fileRepository.findByFileHash(hash).isPresent();
    }
}
