package com.smartshare.service.analytics;

import com.smartshare.exception.analytics.AnalyticsTrackingException;
import com.smartshare.model.entity.analytics.DownloadAnalyticsEntity;
import com.smartshare.repository.analytics.DownloadAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private final DownloadAnalyticsRepository analyticsRepository;

    @Async
    @Transactional
    public void trackDownloadEvent(String shortCode, String fileHash, String userAgent, String ipAddress) {
        try {
            // Basic User-Agent Parsing Placeholder
            String deviceType = "Desktop";
            String browser = "Unknown";
            String os = "Unknown";

            if (userAgent != null) {
                // Device type
                if (userAgent.contains("Mobile") || userAgent.contains("iPhone")) deviceType = "Mobile";
                else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) deviceType = "Tablet";

                // Browser — ORDER MATTERS: Edge and Opera must come before Chrome
                // because their UA strings also contain 'Chrome'
                if (userAgent.contains("Edg/") || userAgent.contains("EdgA/"))     browser = "Edge";
                else if (userAgent.contains("OPR/") || userAgent.contains("Opera")) browser = "Opera";
                else if (userAgent.contains("Chrome"))                               browser = "Chrome";
                else if (userAgent.contains("Firefox"))                              browser = "Firefox";
                else if (userAgent.contains("Safari"))                               browser = "Safari";
                else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) browser = "Internet Explorer";

                // OS
                if (userAgent.contains("Android"))        os = "Android";
                else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";
                else if (userAgent.contains("Windows"))   os = "Windows";
                else if (userAgent.contains("Mac OS X"))  os = "MacOS";
                else if (userAgent.contains("Linux"))     os = "Linux";
            }

            DownloadAnalyticsEntity analytics = DownloadAnalyticsEntity.builder()
                    .shortCode(shortCode)
                    .fileHash(fileHash)
                    .deviceType(deviceType)
                    .browser(browser)
                    .operatingSystem(os)
                    .ipAddress(ipAddress)
                    .build();

            analyticsRepository.save(analytics);
            logger.info("Tracked download event for shortCode {} from {} / {}", shortCode, browser, os);

        } catch (Exception e) {
            logger.error("Failed to track download event for shortCode {}", shortCode, e);
            throw new AnalyticsTrackingException("Database write failure", e);
        }
    }
}
