package com.smartshare.model.dto.analytics.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopFilesDTO {
    private UUID fileId;
    private String fileName;
    private long downloadCount;
    private String fileHash;
}
