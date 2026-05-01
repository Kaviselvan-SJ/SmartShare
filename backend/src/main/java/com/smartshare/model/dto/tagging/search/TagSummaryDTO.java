package com.smartshare.model.dto.tagging.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagSummaryDTO {
    private String tag;
    private long usageCount;
}
