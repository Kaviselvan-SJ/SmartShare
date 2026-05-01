package com.smartshare.model.dto.shortlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkResponseDTO {
    private String shortCode;
    private String shortUrl;
    private LocalDateTime expiryTime;
    private Integer downloadLimit;
}
