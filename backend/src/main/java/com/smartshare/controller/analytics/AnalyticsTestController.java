package com.smartshare.controller.analytics;

import com.smartshare.repository.analytics.DownloadAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/test/analytics")
@RequiredArgsConstructor
public class AnalyticsTestController {

    private final DownloadAnalyticsRepository analyticsRepository;

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        long totalDownloads = analyticsRepository.countByShortCode(shortCode);
        var events = analyticsRepository.findByShortCode(shortCode);

        Map<String, Object> response = new HashMap<>();
        response.put("shortCode", shortCode);
        response.put("totalDownloads", totalDownloads);
        response.put("events", events);

        return ResponseEntity.ok(response);
    }
}
