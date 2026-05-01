package com.smartshare.model.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularTagsDTO {
    private String tag;
    private long usageCount;
}
