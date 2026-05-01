package com.smartshare.security.admin;

import com.smartshare.exception.admin.AdminAccessDeniedException;
import com.smartshare.security.firebase.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AdminAccessValidator {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccessValidator.class);

    @Value("${admin.emails:}")
    private String adminEmailsString;

    public void validateAdminAccess() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (!(principal instanceof AuthenticatedUser)) {
            logger.warn("Unauthorized admin access attempt: no valid authentication principal");
            throw new AdminAccessDeniedException("Access denied: Not authenticated");
        }

        AuthenticatedUser user = (AuthenticatedUser) principal;
        String userEmail = user.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            logger.warn("Unauthorized admin access attempt: user has no email. UID: {}", user.getUid());
            throw new AdminAccessDeniedException("Access denied: Invalid user email");
        }

        List<String> adminEmails = Arrays.asList(adminEmailsString.split(","));
        
        boolean isAdmin = adminEmails.stream()
                .map(String::trim)
                .anyMatch(email -> email.equalsIgnoreCase(userEmail));

        if (!isAdmin) {
            logger.warn("Unauthorized admin access attempt by user {}", userEmail);
            throw new AdminAccessDeniedException("Access denied: User does not have admin privileges");
        }
        
        logger.info("Successful admin validation for user {}", userEmail);
    }
}
