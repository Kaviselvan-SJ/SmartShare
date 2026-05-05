package com.smartshare.controller.file.preview;

import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.file.preview.FilePreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FilePreviewController {

    private final FilePreviewService filePreviewService;

    // Defines MIME types that are safe and natively supported for inline browser preview.
    private static final Set<String> PREVIEWABLE_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "text/plain",
            "application/json"
    );

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<InputStreamResource> previewFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        FilePreviewService.PreviewData previewData = filePreviewService.previewFile(fileId, authenticatedUser.getUid());

        String mimeType = previewData.mimeType;
        // Determine whether to show inline or force download based on supported types
        String dispositionType = PREVIEWABLE_TYPES.contains(mimeType) ? "inline" : "attachment";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + previewData.originalFileName + "\"")
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(new InputStreamResource(previewData.stream));
    }
}
