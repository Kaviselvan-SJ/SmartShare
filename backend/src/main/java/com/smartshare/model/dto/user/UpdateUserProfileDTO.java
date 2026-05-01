package com.smartshare.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileDTO {
    private String displayName;
    private String jobProfile;
    private String organization;
    private String location;
    private String bio;
    private String profileImageUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String experienceLevel;
    private String preferredLanguage;
    private String timezone;
    private Boolean emailNotificationsEnabled;
    private Integer defaultLinkExpiryDays;
}
