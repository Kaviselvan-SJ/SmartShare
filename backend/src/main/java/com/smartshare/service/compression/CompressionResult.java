package com.smartshare.service.compression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompressionResult {
    private InputStream compressedStream;
    private long originalSize;
    private long compressedSize;
    private String compressionFormat;
}
