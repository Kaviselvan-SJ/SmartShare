package com.smartshare.model.dto.shortlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortLinkRequestDTO {
    private UUID fileId;
    private LocalDateTime expiryTime;
    private Integer downloadLimit;
    private String password;
}
