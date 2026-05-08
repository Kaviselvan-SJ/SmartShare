package com.smartshare.security.ratelimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class SlidingWindowRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public SlidingWindowRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int limit, int windowSizeInSeconds) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (windowSizeInSeconds * 1000L);
        String value = UUID.randomUUID().toString();

        // 1. Remove expired timestamps
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 2. Count remaining timestamps
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= limit) {
            return false;
        }

        // 3. Store request timestamp
        redisTemplate.opsForZSet().add(key, value, currentTime);

        // 4. Set TTL to prevent memory leaks
        redisTemplate.expire(key, windowSizeInSeconds, TimeUnit.SECONDS);

        return true;
    }
    
    public void recordFailedAttempt(String key, int windowSizeInSeconds) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (windowSizeInSeconds * 1000L);
        String value = UUID.randomUUID().toString();

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        redisTemplate.opsForZSet().add(key, value, currentTime);
        redisTemplate.expire(key, windowSizeInSeconds, TimeUnit.SECONDS);
    }
    
    public boolean checkLimit(String key, int limit, int windowSizeInSeconds) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (windowSizeInSeconds * 1000L);
        
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(key);
        
        return count == null || count < limit;
    }
    
    public void clear(String key) {
        redisTemplate.delete(key);
    }
}
