package com.smartshare.controller.tagging;

import com.smartshare.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/test/tags")
@RequiredArgsConstructor
public class TaggingTestController {

    private final TagRepository tagRepository;

    @GetMapping("/{fileHash}")
    public ResponseEntity<?> getTags(@PathVariable String fileHash) {
        var tags = tagRepository.findByFileHash(fileHash);
        return ResponseEntity.ok(Map.of("fileHash", fileHash, "tags", tags));
    }
}
