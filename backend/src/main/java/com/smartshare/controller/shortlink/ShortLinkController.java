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
    public ResponseEntity<ShortLinkResponseDTO> createShortLink(@RequestBody CreateShortLinkRequestDTO request) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        ShortLinkResponseDTO response = shortLinkService.createShortLink(request, authenticatedUser.getUid());
        
        return ResponseEntity.ok(response);
    }
}
