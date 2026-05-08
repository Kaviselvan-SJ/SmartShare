package com.smartshare.model.dto.file;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FileVersionDTO {
    private UUID versionId;
    private Integer versionNumber;
    private LocalDateTime uploadedAt;
    private Boolean isCurrentVersion;
    private Long originalSize;
    private Long compressedSize;
    private String previewUrl;
}
