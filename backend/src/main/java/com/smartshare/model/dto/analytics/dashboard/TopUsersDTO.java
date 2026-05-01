package com.smartshare.model.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUsersDTO {
    private String firebaseUid;
    private String email;
    private long totalUploads;
    private long totalDownloads;
}
