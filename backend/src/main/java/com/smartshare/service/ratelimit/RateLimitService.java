package com.smartshare.service.ratelimit;

import com.smartshare.exception.ratelimit.RateLimitExceededException;
import com.smartshare.security.ratelimit.SlidingWindowRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    private final SlidingWindowRateLimiter rateLimiter;

    public RateLimitService(SlidingWindowRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public void checkDownloadLimit(String ip) {
        String key = "ratelimit:download:" + ip;
        if (!rateLimiter.isAllowed(key, 20, 60)) {
            logger.warn("Blocked IP {} for excessive download attempts", ip);
            throw new RateLimitExceededException("Download rate limit exceeded", 60);
        }
    }

    public void checkUploadLimit(String firebaseUid) {
        String key = "ratelimit:upload:" + firebaseUid;
        if (!rateLimiter.isAllowed(key, 10, 60)) {
            logger.warn("Blocked User {} for excessive upload attempts", firebaseUid);
            throw new RateLimitExceededException("Upload rate limit exceeded", 60);
        }
    }

    public void checkPasswordAttempts(String ip) {
        String key = "ratelimit:password:" + ip;
        if (!rateLimiter.checkLimit(key, 5, 600)) {
            logger.warn("Blocked IP {} for excessive failed password attempts", ip);
            throw new RateLimitExceededException("Too many failed password attempts", 600);
        }
    }

    public void recordFailedPasswordAttempt(String ip) {
        String key = "ratelimit:password:" + ip;
        rateLimiter.recordFailedAttempt(key, 600);
        logger.info("Recorded failed password attempt for IP {}", ip);
    }

    public void clearPasswordAttempts(String ip) {
        String key = "ratelimit:password:" + ip;
        rateLimiter.clear(key);
    }

    public void checkAuthLimit(String ip) {
        String key = "ratelimit:auth:" + ip;
        if (!rateLimiter.isAllowed(key, 15, 600)) {
            logger.warn("Blocked IP {} for excessive authentication attempts", ip);
            throw new RateLimitExceededException("Authentication rate limit exceeded", 600);
        }
    }
}
