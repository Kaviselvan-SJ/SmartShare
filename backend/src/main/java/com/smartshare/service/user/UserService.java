package com.smartshare.service.user;

import com.smartshare.model.dto.user.UpdateUserProfileDTO;
import com.smartshare.model.dto.user.UserProfileDTO;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(user -> UserProfileDTO.builder()
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .jobProfile(user.getJobProfile())
                        .organization(user.getOrganization())
                        .location(user.getLocation())
                        .bio(user.getBio())
                        .profileImageUrl(user.getProfileImageUrl())
                        .linkedinUrl(user.getLinkedinUrl())
                        .githubUrl(user.getGithubUrl())
                        .portfolioUrl(user.getPortfolioUrl())
                        .experienceLevel(user.getExperienceLevel())
                        .preferredLanguage(user.getPreferredLanguage())
                        .timezone(user.getTimezone())
                        .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                        .defaultLinkExpiryDays(user.getDefaultLinkExpiryDays())
                        .build())
                .orElse(UserProfileDTO.builder().build()); // Return empty DTO for brand-new users
    }

    @Transactional
    public UserProfileDTO updateUserProfile(String firebaseUid, UpdateUserProfileDTO dto) {
        UserEntity user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate URLs
        if (dto.getLinkedinUrl() != null && !dto.getLinkedinUrl().isEmpty()) {
            if (!dto.getLinkedinUrl().startsWith("https://linkedin.com/")) {
                throw new IllegalArgumentException("LinkedIn URL must start with https://linkedin.com/");
            }
        }

        if (dto.getGithubUrl() != null && !dto.getGithubUrl().isEmpty()) {
            if (!dto.getGithubUrl().startsWith("https://github.com/")) {
                throw new IllegalArgumentException("GitHub URL must start with https://github.com/");
            }
        }

        if (dto.getPortfolioUrl() != null && !dto.getPortfolioUrl().isEmpty()) {
            if (!dto.getPortfolioUrl().startsWith("https://")) {
                throw new IllegalArgumentException("Portfolio URL must start with https://");
            }
        }

        // Limit displayName length
        if (dto.getDisplayName() != null && dto.getDisplayName().length() > 50) {
            user.setDisplayName(dto.getDisplayName().substring(0, 50));
        } else {
            user.setDisplayName(dto.getDisplayName());
        }

        // Sanitize bio length
        if (dto.getBio() != null && dto.getBio().length() > 500) {
            user.setBio(dto.getBio().substring(0, 500));
        } else {
            user.setBio(dto.getBio());
        }

        user.setJobProfile(dto.getJobProfile());
        user.setOrganization(dto.getOrganization());
        user.setLocation(dto.getLocation());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setLinkedinUrl(dto.getLinkedinUrl());
        user.setGithubUrl(dto.getGithubUrl());
        user.setPortfolioUrl(dto.getPortfolioUrl());
        user.setExperienceLevel(dto.getExperienceLevel());
        user.setPreferredLanguage(dto.getPreferredLanguage());
        user.setTimezone(dto.getTimezone());
        user.setEmailNotificationsEnabled(dto.getEmailNotificationsEnabled());
        user.setDefaultLinkExpiryDays(dto.getDefaultLinkExpiryDays());

        user = userRepository.save(user);

        return UserProfileDTO.builder()
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .jobProfile(user.getJobProfile())
                .organization(user.getOrganization())
                .location(user.getLocation())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .githubUrl(user.getGithubUrl())
                .portfolioUrl(user.getPortfolioUrl())
                .experienceLevel(user.getExperienceLevel())
                .preferredLanguage(user.getPreferredLanguage())
                .timezone(user.getTimezone())
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .defaultLinkExpiryDays(user.getDefaultLinkExpiryDays())
                .build();
    }
}
