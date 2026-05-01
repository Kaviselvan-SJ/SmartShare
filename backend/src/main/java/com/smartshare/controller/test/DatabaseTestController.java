package com.smartshare.controller.test;

import com.smartshare.model.entity.UserEntity;
import com.smartshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/test")
@RequiredArgsConstructor
public class DatabaseTestController {

    private final UserRepository userRepository;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Create a test user
            UserEntity testUser = UserEntity.builder()
                    .firebaseUid("test-uid-" + UUID.randomUUID())
                    .email("test-" + UUID.randomUUID() + "@example.com")
                    .build();

            // Save to database
            UserEntity savedUser = userRepository.save(testUser);

            // Retrieve from database
            UserEntity retrievedUser = userRepository.findById(savedUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found after saving"));

            response.put("status", "success");
            response.put("message", "Database connection and entity persistence working perfectly!");
            response.put("savedUser", retrievedUser);

            // Clean up the test user to avoid clutter
            userRepository.delete(retrievedUser);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
