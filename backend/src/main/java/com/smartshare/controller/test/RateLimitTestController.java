package com.smartshare.controller.test;

import com.smartshare.service.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("!prod")
@RestController
@RequestMapping("/api/test/ratelimit")
public class RateLimitTestController {

    private final RateLimitService rateLimitService;

    public RateLimitTestController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/download")
    public ResponseEntity<String> testDownloadRateLimit(HttpServletRequest request) {
        String ip = extractIpAddress(request);
        rateLimitService.checkDownloadLimit(ip);
        return ResponseEntity.ok("Download allowed. IP: " + ip);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> testUploadRateLimit(HttpServletRequest request) {
        // For testing, we simulate a firebaseUid based on IP if not authenticated
        String simulatedUid = "test-uid-" + extractIpAddress(request);
        rateLimitService.checkUploadLimit(simulatedUid);
        return ResponseEntity.ok("Upload allowed. Uid: " + simulatedUid);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
