package com.smartshare.controller.shortlink;

import com.smartshare.model.dto.shortlink.CreateShortLinkRequestDTO;
import com.smartshare.model.dto.shortlink.ShortLinkResponseDTO;
import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.shortlink.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shortlinks")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping("/create")
    public ResponseEntity<?> createShortLink(@RequestBody CreateShortLinkRequestDTO request) {
        try {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            ShortLinkResponseDTO response = shortLinkService.createShortLink(request, authenticatedUser.getUid());
            return ResponseEntity.ok(response);
        } catch (com.smartshare.exception.shortlink.ShortLinkCreationException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("message", "An unexpected error occurred"));
        }
    }
    @org.springframework.web.bind.annotation.DeleteMapping("/{shortCode}")
    public ResponseEntity<?> deleteShortLink(@org.springframework.web.bind.annotation.PathVariable String shortCode) {
        try {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            shortLinkService.deleteShortLink(shortCode, authenticatedUser.getUid());
            return ResponseEntity.ok(java.util.Map.of("message", "Short link and associated analytics successfully deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
