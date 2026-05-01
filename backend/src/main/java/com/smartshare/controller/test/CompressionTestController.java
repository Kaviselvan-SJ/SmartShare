package com.smartshare.controller.test;

import com.smartshare.service.compression.CompressionResult;
import com.smartshare.service.compression.CompressionService;
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
@RequestMapping("/api/public/test/compress")
@RequiredArgsConstructor
public class CompressionTestController {

    private final CompressionService compressionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> compressFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            CompressionResult result = compressionService.compressFile(file.getInputStream(), file.getOriginalFilename());
            
            // Close the stream immediately since we are just testing compression stats
            // The temp file will be automatically deleted by our custom stream implementation
            result.getCompressedStream().close();
            
            response.put("status", "success");
            response.put("originalSize", result.getOriginalSize());
            response.put("compressedSize", result.getCompressedSize());
            
            double ratio = compressionService.calculateCompressionRatio(result.getOriginalSize(), result.getCompressedSize());
            response.put("compressionRatio", String.format("%.2f%%", ratio * 100));
            response.put("compressionFormat", result.getCompressionFormat());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
