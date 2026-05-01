package com.smartshare.controller.user;

import com.smartshare.model.dto.user.UpdateUserProfileDTO;
import com.smartshare.model.dto.user.UserProfileDTO;
import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        UserProfileDTO profile = userService.getUserProfile(authenticatedUser.getUid());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody UpdateUserProfileDTO updateDTO) {
        try {
            UserProfileDTO updatedProfile = userService.updateUserProfile(authenticatedUser.getUid(), updateDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }
}
