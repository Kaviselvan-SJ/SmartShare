package com.smartshare.service.tagging.search;

import com.smartshare.exception.tagging.search.TagSearchException;
import com.smartshare.model.dto.tagging.search.TagSummaryDTO;
import com.smartshare.model.dto.tagging.search.TaggedFileDTO;
import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.model.entity.tag.TagEntity;
import com.smartshare.repository.FileRepository;
import com.smartshare.repository.UserRepository;
import com.smartshare.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagSearchService {

    private static final Logger logger = LoggerFactory.getLogger(TagSearchService.class);

    private final TagRepository tagRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaggedFileDTO> getFilesByTag(String tag, String firebaseUid) {
        try {
            if (tag == null || tag.isEmpty()) {
                throw new TagSearchException("Tag cannot be empty");
            }

            UserEntity user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new TagSearchException("User not found"));

            List<TagEntity> tagEntities = tagRepository.findByTag(tag.toLowerCase());
            Set<String> matchingFileHashes = tagEntities.stream()
                    .map(TagEntity::getFileHash)
                    .collect(Collectors.toSet());

            List<FileEntity> userFiles = fileRepository.findByOwnerOrderByCreatedAtDesc(user);
            List<FileEntity> filteredFiles = userFiles.stream()
                    .filter(file -> matchingFileHashes.contains(file.getFileHash()))
                    .collect(Collectors.toList());

            logger.info("User searched tag {}, {} files returned", tag, filteredFiles.size());
            return mapToFileDTOs(filteredFiles);
        } catch (TagSearchException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to execute tag search", e);
            throw new TagSearchException("Database query failure", e);
        }
    }

    @Transactional(readOnly = true)
    public List<TaggedFileDTO> getFilesByMultipleTags(List<String> tags, String firebaseUid) {
        try {
            if (tags == null || tags.isEmpty()) {
                throw new TagSearchException("Tag list cannot be empty");
            }

            UserEntity user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new TagSearchException("User not found"));

            Set<String> intersectedHashes = null;

            for (String tag : tags) {
                Set<String> hashesForTag = tagRepository.findByTag(tag.toLowerCase()).stream()
                        .map(TagEntity::getFileHash)
                        .collect(Collectors.toSet());

                if (intersectedHashes == null) {
                    intersectedHashes = new HashSet<>(hashesForTag);
                } else {
                    intersectedHashes.retainAll(hashesForTag);
                }

                if (intersectedHashes.isEmpty()) {
                    break; // Early exit if intersection is empty
                }
            }

            if (intersectedHashes == null || intersectedHashes.isEmpty()) {
                logger.info("User searched tags {}, 0 files returned", tags);
                return Collections.emptyList();
            }

            Set<String> finalHashes = intersectedHashes;
            List<FileEntity> userFiles = fileRepository.findByOwnerOrderByCreatedAtDesc(user);
            List<FileEntity> filteredFiles = userFiles.stream()
                    .filter(file -> finalHashes.contains(file.getFileHash()))
                    .collect(Collectors.toList());

            logger.info("User searched tags {}, {} files returned", tags, filteredFiles.size());
            return mapToFileDTOs(filteredFiles);

        } catch (TagSearchException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to execute multi-tag search", e);
            throw new TagSearchException("Database query failure", e);
        }
    }

    @Transactional(readOnly = true)
    public List<TagSummaryDTO> getUserTags(String firebaseUid) {
        try {
            UserEntity user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new TagSearchException("User not found"));

            List<String> userFileHashes = fileRepository.findByOwnerOrderByCreatedAtDesc(user).stream()
                    .map(FileEntity::getFileHash)
                    .collect(Collectors.toList());

            if (userFileHashes.isEmpty()) {
                return Collections.emptyList();
            }

            List<TagEntity> allTags = tagRepository.findByFileHashIn(userFileHashes);

            Map<String, Long> tagCounts = allTags.stream()
                    .collect(Collectors.groupingBy(TagEntity::getTag, Collectors.counting()));

            return tagCounts.entrySet().stream()
                    .map(entry -> new TagSummaryDTO(entry.getKey(), entry.getValue()))
                    .sorted((a, b) -> Long.compare(b.getUsageCount(), a.getUsageCount()))
                    .collect(Collectors.toList());

        } catch (TagSearchException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve user tags", e);
            throw new TagSearchException("Database query failure", e);
        }
    }

    @Transactional(readOnly = true)
    public List<TagSummaryDTO> getPopularTags() {
        try {
            List<Object[]> results = tagRepository.findPopularTags(PageRequest.of(0, 20));

            return results.stream()
                    .map(row -> new TagSummaryDTO((String) row[0], (Long) row[1]))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to retrieve popular tags", e);
            throw new TagSearchException("Database query failure", e);
        }
    }

    private List<TaggedFileDTO> mapToFileDTOs(List<FileEntity> files) {
        if (files.isEmpty()) return Collections.emptyList();

        List<String> fileHashes = files.stream()
                .map(FileEntity::getFileHash)
                .collect(Collectors.toList());

        List<TagEntity> allTags = tagRepository.findByFileHashIn(fileHashes);
        Map<String, List<String>> tagsByHash = allTags.stream()
                .collect(Collectors.groupingBy(
                        TagEntity::getFileHash,
                        Collectors.mapping(TagEntity::getTag, Collectors.toList())
                ));

        return files.stream().map(file -> TaggedFileDTO.builder()
                .fileId(file.getId())
                .fileName(file.getFileName())
                .fileHash(file.getFileHash())
                .ownerFirebaseUid(file.getOwner().getFirebaseUid())
                .createdAt(file.getCreatedAt())
                .tags(tagsByHash.getOrDefault(file.getFileHash(), Collections.emptyList()))
                .build()).collect(Collectors.toList());
    }
}
