package com.smartshare.service.compression;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompressionStrategyFactory {

    private final GzipCompressionStrategy gzipCompressionStrategy;

    public CompressionStrategy getStrategy(String fileName) {
        if (fileName == null) {
            return gzipCompressionStrategy;
        }
        
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".txt") || lowerName.endsWith(".json") || lowerName.endsWith(".log")) {
            return gzipCompressionStrategy;
        }
        
        // Default strategy for initial implementation is always gzip
        // Can be expanded to return NoOp strategy for videos/images later
        return gzipCompressionStrategy;
    }
}
