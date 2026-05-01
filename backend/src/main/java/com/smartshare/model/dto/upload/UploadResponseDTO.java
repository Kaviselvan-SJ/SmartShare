package com.smartshare.model.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {
    private UUID fileId;
    private String fileName;
    private String fileHash;
    private long originalSize;
    private long compressedSize;
    private boolean duplicate;
    private String message;
}
