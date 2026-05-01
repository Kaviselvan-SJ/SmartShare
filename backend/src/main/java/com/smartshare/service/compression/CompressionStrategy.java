package com.smartshare.service.compression;

import java.io.InputStream;

public interface CompressionStrategy {
    
    /**
     * Compresses the input stream and returns the compression result.
     * 
     * @param inputStream the original file stream
     * @param fileName the original file name
     * @return CompressionResult containing the compressed stream and metadata
     */
    CompressionResult compress(InputStream inputStream, String fileName);
    
    /**
     * @return the format identifier for this strategy
     */
    String getFormat();
}
