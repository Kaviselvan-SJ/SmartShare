package com.smartshare.service.cache;

import com.smartshare.exception.cache.CacheException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    private static final String KEY_PREFIX = "shortlink:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheStoragePath(String shortCode, String storagePath) {
        try {
            String key = KEY_PREFIX + shortCode;
            redisTemplate.opsForValue().set(key, storagePath, DEFAULT_TTL);
            logger.info("Stored mapping in Redis for shortCode {}", shortCode);
        } catch (Exception e) {
            logger.error("Failed to cache short link mapping", e);
            throw new CacheException("Failed to store mapping in Redis", e);
        }
    }

    public String retrieveStoragePath(String shortCode) {
        try {
            String key = KEY_PREFIX + shortCode;
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value != null) {
                logger.info("Cache hit for shortCode {}", shortCode);
                return value.toString();
            } else {
                logger.info("Cache miss for shortCode {}", shortCode);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve short link mapping from cache", e);
            throw new CacheException("Failed to retrieve mapping from Redis", e);
        }
    }

    public void deleteStoragePath(String shortCode) {
        try {
            String key = KEY_PREFIX + shortCode;
            redisTemplate.delete(key);
            logger.info("Cache delete for shortCode {}", shortCode);
        } catch (Exception e) {
            logger.error("Failed to delete short link mapping from cache", e);
            throw new CacheException("Failed to delete mapping from Redis", e);
        }
    }

    public void clearCache() {
        try {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
            logger.info("Cleared entire Redis cache");
        } catch (Exception e) {
            logger.error("Failed to clear Redis cache", e);
            throw new CacheException("Failed to clear Redis cache", e);
        }
    }
}
