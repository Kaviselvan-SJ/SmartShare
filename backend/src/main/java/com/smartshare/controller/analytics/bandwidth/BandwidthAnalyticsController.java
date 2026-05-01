package com.smartshare.controller.analytics.bandwidth;

import com.smartshare.exception.analytics.bandwidth.BandwidthAnalyticsException;
import com.smartshare.model.dto.analytics.BandwidthSavingsDTO;
import com.smartshare.model.dto.analytics.SystemBandwidthSavingsDTO;
import com.smartshare.model.dto.analytics.UserBandwidthSavingsDTO;
import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.analytics.bandwidth.BandwidthAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics/bandwidth")
@RequiredArgsConstructor
public class BandwidthAnalyticsController {

    private final BandwidthAnalyticsService bandwidthAnalyticsService;

    @GetMapping("/file/{fileId}")
    public ResponseEntity<?> getFileSavings(@PathVariable UUID fileId) {
        try {
            BandwidthSavingsDTO dto = bandwidthAnalyticsService.calculateFileSavings(fileId);
            return ResponseEntity.ok(dto);
        } catch (BandwidthAnalyticsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserSavings() {
        try {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserBandwidthSavingsDTO dto = bandwidthAnalyticsService.calculateUserSavings(authenticatedUser.getUid());
            return ResponseEntity.ok(dto);
        } catch (BandwidthAnalyticsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/system")
    public ResponseEntity<?> getSystemSavings() {
        try {
            SystemBandwidthSavingsDTO dto = bandwidthAnalyticsService.calculateTotalSystemSavings();
            return ResponseEntity.ok(dto);
        } catch (BandwidthAnalyticsException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
