package com.smartshare.controller.analytics.dashboard;

import com.smartshare.exception.analytics.dashboard.DashboardAnalyticsException;
import com.smartshare.service.analytics.dashboard.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics/dashboard")
@RequiredArgsConstructor
public class DashboardAnalyticsController {

    private final DashboardAnalyticsService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<?> getSystemOverview() {
        try {
            return ResponseEntity.ok(dashboardService.getSystemOverview());
        } catch (DashboardAnalyticsException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/top-files")
    public ResponseEntity<?> getTopFiles() {
        try {
            return ResponseEntity.ok(dashboardService.getTopDownloadedFiles());
        } catch (DashboardAnalyticsException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/popular-tags")
    public ResponseEntity<?> getPopularTags() {
        try {
            return ResponseEntity.ok(dashboardService.getPopularTags());
        } catch (DashboardAnalyticsException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity() {
        try {
            return ResponseEntity.ok(dashboardService.getRecentActivity());
        } catch (DashboardAnalyticsException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
