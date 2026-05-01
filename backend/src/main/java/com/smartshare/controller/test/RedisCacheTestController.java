package com.smartshare.controller.test;

import com.smartshare.service.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/test/cache")
@RequiredArgsConstructor
public class RedisCacheTestController {

    private final RedisCacheService redisCacheService;

    @PostMapping("/{shortCode}")
    public ResponseEntity<Map<String, String>> storeMapping(@PathVariable String shortCode, @RequestParam String storagePath) {
        redisCacheService.cacheStoragePath(shortCode, storagePath);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Mapping stored successfully");
        response.put("shortCode", shortCode);
        response.put("storagePath", storagePath);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Map<String, String>> retrieveMapping(@PathVariable String shortCode) {
        String storagePath = redisCacheService.retrieveStoragePath(shortCode);
        Map<String, String> response = new HashMap<>();
        if (storagePath != null) {
            response.put("status", "success");
            response.put("shortCode", shortCode);
            response.put("storagePath", storagePath);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "miss");
            response.put("message", "Cache miss for short code");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Map<String, String>> deleteMapping(@PathVariable String shortCode) {
        redisCacheService.deleteStoragePath(shortCode);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Mapping deleted successfully");
        return ResponseEntity.ok(response);
    }
}
