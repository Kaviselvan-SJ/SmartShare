package com.smartshare.service.compression;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class CompressionService {

    private static final Logger logger = LoggerFactory.getLogger(CompressionService.class);

    private final CompressionStrategyFactory strategyFactory;

    public CompressionResult compressFile(InputStream inputStream, String fileName) {
        CompressionStrategy strategy = strategyFactory.getStrategy(fileName);
        
        CompressionResult result = strategy.compress(inputStream, fileName);
        
        double ratio = calculateCompressionRatio(result.getOriginalSize(), result.getCompressedSize());
        
        logger.info("Compressed file {} from {} bytes to {} bytes ({}% reduction)", 
                fileName, 
                result.getOriginalSize(), 
                result.getCompressedSize(), 
                String.format("%.1f", ratio * 100));
                
        return result;
    }

    public double calculateCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize <= 0) {
            return 0.0;
        }
        return (double) (originalSize - compressedSize) / originalSize;
    }
}
