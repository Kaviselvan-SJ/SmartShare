package com.smartshare.service.cache;

import com.smartshare.exception.cache.CacheException;
import com.smartshare.model.entity.ShortLinkEntity;
import com.smartshare.repository.ShortLinkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShortLinkCacheResolver {

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkCacheResolver.class);

    private final RedisCacheService redisCacheService;
    private final ShortLinkRepository shortLinkRepository;

    public String resolveStoragePath(String shortCode) {
        // Step 1: Check Redis cache
        String cachedPath = redisCacheService.retrieveStoragePath(shortCode);
        if (cachedPath != null) {
            return cachedPath;
        }

        // Step 2: Cache miss, query database
        Optional<ShortLinkEntity> entityOpt = shortLinkRepository.findByShortCode(shortCode);
        
        if (entityOpt.isPresent()) {
            String storagePath = entityOpt.get().getFile().getStoragePath();
            
            // Step 3: Store result in Redis
            redisCacheService.cacheStoragePath(shortCode, storagePath);
            return storagePath;
        }

        throw new CacheException("Short link not found for code: " + shortCode);
    }
}
