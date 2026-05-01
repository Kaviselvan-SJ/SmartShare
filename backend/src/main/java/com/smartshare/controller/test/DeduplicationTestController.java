package com.smartshare.controller.test;

import com.smartshare.service.deduplication.DeduplicationResult;
import com.smartshare.service.deduplication.DeduplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/test/dedup")
@RequiredArgsConstructor
public class DeduplicationTestController {

    private final DeduplicationService deduplicationService;

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkDuplicate(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            DeduplicationResult result = deduplicationService.checkDuplicate(file.getInputStream());
            
            response.put("status", "success");
            response.put("hash", result.getFileHash());
            response.put("duplicateFound", result.isDuplicateFound());
            
            if (result.isDuplicateFound()) {
                response.put("existingStoragePath", result.getExistingStoragePath());
                response.put("existingFileId", result.getExistingFileId());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
