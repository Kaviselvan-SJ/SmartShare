package com.smartshare.controller.download;

import com.smartshare.exception.download.DownloadException;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.service.download.DownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/f")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String shortCode,
            @RequestHeader(value = "X-Download-Password", required = false) String password) {
            
        try {
            // Retrieve metadata to get original filename
            FileEntity metadata = downloadService.getFileMetadata(shortCode);
            
            // Process the download pipeline
            InputStream fileStream = downloadService.processDownload(shortCode, password);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(fileStream));
                    
        } catch (DownloadException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
