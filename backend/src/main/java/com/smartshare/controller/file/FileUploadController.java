package com.smartshare.controller.file;

import com.smartshare.model.dto.upload.UploadResponseDTO;
import com.smartshare.service.upload.FileUploadService;
import com.smartshare.service.file.FileDeletionService;
import com.smartshare.service.file.FileDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final FileDeletionService fileDeletionService;
    private final FileDetailsService fileDetailsService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        UploadResponseDTO response = fileUploadService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.GetMapping("/my-files")
    public ResponseEntity<java.util.List<UploadResponseDTO>> getMyFiles() {
        return ResponseEntity.ok(fileUploadService.getMyFiles());
    }

    @org.springframework.web.bind.annotation.GetMapping("/{fileId}/details")
    public ResponseEntity<com.smartshare.model.dto.file.details.FileDetailsDTO> getFileDetails(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID fileId) {
        com.smartshare.security.firebase.AuthenticatedUser user = 
            (com.smartshare.security.firebase.AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(fileDetailsService.getFileDetails(fileId, user.getUid()));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{fileId}")
    public ResponseEntity<java.util.Map<String, Object>> deleteFile(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID fileId) {
        com.smartshare.security.firebase.AuthenticatedUser user = 
            (com.smartshare.security.firebase.AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        fileDeletionService.deleteFile(fileId, user.getUid());
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("deletedFileId", fileId);
        response.put("message", "File and all associated metadata successfully deleted");
        
        return ResponseEntity.ok(response);
    }
}
