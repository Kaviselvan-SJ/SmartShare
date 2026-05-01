package com.smartshare.service.deduplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeduplicationResult {
    private boolean duplicateFound;
    private String fileHash;
    private String existingStoragePath;
    private UUID existingFileId;
}
