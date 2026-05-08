package com.smartshare.controller.file;

import com.smartshare.model.dto.file.FileVersionDTO;
import com.smartshare.model.dto.upload.UploadResponseDTO;
import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.file.FileVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileVersionController {

    private final FileVersionService fileVersionService;

    @GetMapping("/check-duplicate")
    public ResponseEntity<UploadResponseDTO> checkDuplicate(
            @RequestParam String fileName,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(fileVersionService.checkDuplicate(fileName, authenticatedUser));
    }

    @PostMapping("/{fileGroupId}/versions")
    public ResponseEntity<UploadResponseDTO> uploadNewVersion(
            @PathVariable UUID fileGroupId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean replace,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        // AuthenticatedUser is retrieved from SecurityContext in service
        return ResponseEntity.ok(fileVersionService.addVersion(fileGroupId, file, replace));
    }

    @PutMapping("/{fileGroupId}/current-version/{versionId}")
    public ResponseEntity<Void> switchCurrentVersion(
            @PathVariable UUID fileGroupId,
            @PathVariable UUID versionId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        fileVersionService.switchCurrentVersion(fileGroupId, versionId, authenticatedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{fileGroupId}/versions")
    public ResponseEntity<List<FileVersionDTO>> getVersionHistory(
            @PathVariable UUID fileGroupId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(fileVersionService.getVersionHistory(fileGroupId, authenticatedUser));
    }
}
