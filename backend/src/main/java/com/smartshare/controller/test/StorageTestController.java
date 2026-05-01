package com.smartshare.controller.test;

import com.smartshare.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/test/storage")
@RequiredArgsConstructor
public class StorageTestController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            // Use a random UUID for testing purposes to mimic a hash
            String objectName = UUID.randomUUID().toString().replace("-", "");
            
            storageService.uploadFile(
                    objectName, 
                    file.getInputStream(), 
                    file.getSize(), 
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"
            );
            
            response.put("status", "success");
            response.put("message", "File uploaded successfully to MinIO");
            response.put("objectName", objectName);
            response.put("url", storageService.generateObjectUrl(objectName));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectName) {
        try {
            InputStream stream = storageService.downloadFile(objectName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
