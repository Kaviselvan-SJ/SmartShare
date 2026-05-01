package com.smartshare.controller.tagging.search;

import com.smartshare.exception.tagging.search.TagSearchException;
import com.smartshare.model.dto.tagging.search.TagSummaryDTO;
import com.smartshare.model.dto.tagging.search.TaggedFileDTO;
import com.smartshare.security.firebase.AuthenticatedUser;
import com.smartshare.service.tagging.search.TagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagSearchController {

    private final TagSearchService tagSearchService;

    @GetMapping("/search/{tag}")
    public ResponseEntity<?> searchBySingleTag(@PathVariable String tag) {
        try {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<TaggedFileDTO> results = tagSearchService.getFilesByTag(tag, authenticatedUser.getUid());
            return ResponseEntity.ok(results);
        } catch (TagSearchException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByMultipleTags(@RequestParam("tags") String tagsParam) {
        try {
            if (tagsParam == null || tagsParam.trim().isEmpty()) {
                throw new TagSearchException("Tags parameter is required");
            }
            
            List<String> tagList = Arrays.stream(tagsParam.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());

            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<TaggedFileDTO> results = tagSearchService.getFilesByMultipleTags(tagList, authenticatedUser.getUid());
            return ResponseEntity.ok(results);
        } catch (TagSearchException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserTagSummary() {
        try {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<TagSummaryDTO> results = tagSearchService.getUserTags(authenticatedUser.getUid());
            return ResponseEntity.ok(results);
        } catch (TagSearchException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularTags() {
        try {
            List<TagSummaryDTO> results = tagSearchService.getPopularTags();
            return ResponseEntity.ok(results);
        } catch (TagSearchException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
