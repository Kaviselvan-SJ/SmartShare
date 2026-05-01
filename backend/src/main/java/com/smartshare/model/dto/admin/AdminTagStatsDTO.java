package com.smartshare.model.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminTagStatsDTO {
    private String tag;
    private long usageCount;
}
