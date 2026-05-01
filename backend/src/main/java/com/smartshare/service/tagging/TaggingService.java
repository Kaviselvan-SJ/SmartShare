package com.smartshare.service.tagging;

import com.smartshare.exception.tagging.TaggingException;
import com.smartshare.model.entity.tag.TagEntity;
import com.smartshare.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaggingService {

    private static final Logger logger = LoggerFactory.getLogger(TaggingService.class);
    private final TagRepository tagRepository;

    @Transactional
    public List<String> generateTags(String fileName, String fileHash) {
        if (fileName == null || fileName.isEmpty()) {
            throw new TaggingException("Invalid filename");
        }

        try {
            Set<String> generatedTags = new HashSet<>();

            // 1. Extract Extension Tag
            String extension = extractExtension(fileName);
            if (extension != null) {
                generatedTags.add(mapExtensionToTag(extension));
            }

            // 2. Tokenize Filename
            String nameWithoutExt = removeExtension(fileName);
            String[] tokens = nameWithoutExt.split("[_\\-\\s\\.]+");

            // 3. Keyword Classification Rules
            for (String token : tokens) {
                String normalized = token.toLowerCase().trim();
                if (normalized.length() >= 2) {
                    generatedTags.add(normalized); // Add base token
                    String mappedKeyword = mapKeywordToTag(normalized);
                    if (mappedKeyword != null) {
                        generatedTags.add(mappedKeyword);
                    }
                }
            }

            // Convert to list to save and return
            List<String> finalTags = new ArrayList<>(generatedTags);

            // Store tags in database
            List<TagEntity> tagEntities = finalTags.stream()
                    .map(tag -> TagEntity.builder()
                            .fileHash(fileHash)
                            .tag(tag)
                            .build())
                    .collect(Collectors.toList());

            tagRepository.saveAll(tagEntities);

            logger.info("Generated tags for {}: {}", fileName, String.join(", ", finalTags));
            return finalTags;

        } catch (Exception e) {
            logger.error("Failed to generate tags for file {}", fileName, e);
            throw new TaggingException("Tag parsing or database write failure", e);
        }
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }

    private String removeExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private String mapExtensionToTag(String extension) {
        return switch (extension) {
            case "pdf" -> "pdf";
            case "jpg", "jpeg", "png", "gif", "webp" -> "image";
            case "json" -> "json";
            case "txt" -> "text";
            case "log" -> "logs";
            case "csv", "xlsx", "xls" -> "spreadsheet";
            case "mp4", "mkv", "avi" -> "video";
            case "mp3", "wav" -> "audio";
            default -> extension;
        };
    }

    private String mapKeywordToTag(String keyword) {
        return switch (keyword) {
            case "assignment" -> "academic";
            case "invoice" -> "finance";
            case "report" -> "business";
            case "notes" -> "personal";
            case "resume", "cv" -> "career";
            case "dataset" -> "data-science";
            case "ml" -> "machine-learning";
            case "ai" -> "artificial-intelligence";
            default -> null;
        };
    }
}
