package com.smartshare.service.shortlink;

import com.smartshare.exception.shortlink.ShortLinkCreationException;
import com.smartshare.model.dto.shortlink.CreateShortLinkRequestDTO;
import com.smartshare.model.dto.shortlink.ShortLinkResponseDTO;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.ShortLinkEntity;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.ShortLinkRepository;
import com.smartshare.repository.UserRepository;
import com.smartshare.service.cache.RedisCacheService;
import com.smartshare.util.shortlink.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkService.class);

    private final ShortLinkRepository shortLinkRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final RedisCacheService redisCacheService;

    @Value("${spring.application.name:smartshare}")
    private String appName;

    // For now, hardcode base URL or pull from config. We will assume http://localhost:8080
    // In production, this would be an environment variable like ${app.base-url}
    private final String baseUrl = "http://localhost:8080/f/";

    @Transactional
    public ShortLinkResponseDTO createShortLink(CreateShortLinkRequestDTO request, String firebaseUid) {
        try {
            // 1. Retrieve user
            UserEntity user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new ShortLinkCreationException("User not found"));

            // 2. Retrieve file
            FileEntity file = fileRepository.findById(request.getFileId())
                    .orElseThrow(() -> new ShortLinkCreationException("File not found"));

            // 3. Ensure file ownership
            if (!file.getOwner().getId().equals(user.getId())) {
                throw new ShortLinkCreationException("Unauthorized access to file");
            }

            // 4. Validate expiry timestamp if provided
            if (request.getExpiryTime() != null && request.getExpiryTime().isBefore(LocalDateTime.now())) {
                throw new ShortLinkCreationException("Expiry time cannot be in the past");
            }

            // 5. Generate unique short code
            String shortCode = shortCodeGenerator.generateUniqueCode();

            // 6. Create and save entity
            ShortLinkEntity shortLink = ShortLinkEntity.builder()
                    .shortCode(shortCode)
                    .file(file)
                    .expiryTime(request.getExpiryTime())
                    .downloadLimit(request.getDownloadLimit())
                    .password(request.getPassword())
                    .downloadCount(0)
                    .build();

            shortLinkRepository.save(shortLink);

            // 6.5 Store mapping in Redis cache for fast resolution
            redisCacheService.cacheStoragePath(shortCode, file.getStoragePath());

            logger.info("Short link created for file {} with code {}", file.getId(), shortCode);

            // 7. Build response
            return ShortLinkResponseDTO.builder()
                    .shortCode(shortCode)
                    .shortUrl(baseUrl + shortCode)
                    .expiryTime(shortLink.getExpiryTime())
                    .downloadLimit(shortLink.getDownloadLimit())
                    .build();

        } catch (ShortLinkCreationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create short link", e);
            throw new ShortLinkCreationException("Failed to generate short link: " + e.getMessage(), e);
        }
    }
}
