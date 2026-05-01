package com.smartshare.controller.admin;

import com.smartshare.exception.admin.AdminAccessDeniedException;
import com.smartshare.security.admin.AdminAccessValidator;
import com.smartshare.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AdminAccessValidator adminAccessValidator;

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview() {
        try {
            adminAccessValidator.validateAdminAccess();
            return ResponseEntity.ok(adminDashboardService.getAdminOverview());
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usage-trends")
    public ResponseEntity<?> getUsageTrends() {
        try {
            adminAccessValidator.validateAdminAccess();
            return ResponseEntity.ok(adminDashboardService.getUsageTrends());
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/popular-tags")
    public ResponseEntity<?> getPopularTags() {
        try {
            adminAccessValidator.validateAdminAccess();
            return ResponseEntity.ok(adminDashboardService.getPopularTags());
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/storage-stats")
    public ResponseEntity<?> getStorageStats() {
        try {
            adminAccessValidator.validateAdminAccess();
            return ResponseEntity.ok(adminDashboardService.getStorageStats());
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity() {
        try {
            adminAccessValidator.validateAdminAccess();
            return ResponseEntity.ok(adminDashboardService.getRecentSystemActivity());
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
