package com.smartshare.service.storage;

import java.io.InputStream;

public interface StorageService {
    
    void uploadFile(String objectName, InputStream stream, long size, String contentType);
    
    InputStream downloadFile(String objectName);
    
    void deleteFile(String objectName);
    
    boolean objectExists(String objectName);
    
    String generateObjectUrl(String objectName);
}
