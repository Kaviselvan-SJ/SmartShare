package com.smartshare.model.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminUsageStatsDTO {
    private Map<String, Long> uploadsPerDay;
    private Map<String, Long> downloadsPerDay;
    private Map<String, Long> newUsersPerDay;
}
