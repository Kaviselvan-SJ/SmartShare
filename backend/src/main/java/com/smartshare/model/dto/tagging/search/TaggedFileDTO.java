package com.smartshare.model.dto.tagging.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggedFileDTO {
    private UUID fileId;
    private String fileName;
    private String fileHash;
    private String ownerFirebaseUid;
    private LocalDateTime createdAt;
    private List<String> tags;
}
