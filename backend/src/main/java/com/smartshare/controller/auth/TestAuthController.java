package com.smartshare.controller.auth;

import com.smartshare.security.firebase.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestAuthController {

    @GetMapping("/authenticated")
    public ResponseEntity<Map<String, String>> testAuthenticatedRoute(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        
        Map<String, String> response = new HashMap<>();
        response.put("uid", user.getUid());
        response.put("email", user.getEmail());
        response.put("message", "You have successfully accessed a protected route!");
        
        return ResponseEntity.ok(response);
    }
}
