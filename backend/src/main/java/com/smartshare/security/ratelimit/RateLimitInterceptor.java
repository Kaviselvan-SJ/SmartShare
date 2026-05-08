package com.smartshare.security.ratelimit;

import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String ip = extractIpAddress(request);

        if (uri.startsWith("/f/")) {
            // Apply download rate limit only.
            // Password attempt blocking is handled inside DownloadController
            // where the actual auth event (failure/success) is known.
            rateLimitService.checkDownloadLimit(ip);
            
        } else if (uri.startsWith("/api/files/upload")) {
            // Extract firebaseUid
            String firebaseUid = extractFirebaseUid();
            if (firebaseUid != null) {
                rateLimitService.checkUploadLimit(firebaseUid);
            }
        } else if (uri.startsWith("/auth/")) {
            // Apply authentication rate limit
            rateLimitService.checkAuthLimit(ip);
        }

        return true;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractFirebaseUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
            return user.getUid();
        }
        return null;
    }
}
