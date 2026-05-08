package com.smartshare.migration;

import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.FileGroupEntity;
import com.smartshare.repository.FileGroupRepository;
import com.smartshare.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataMigrationRunner.class);

    private final FileRepository fileRepository;
    private final FileGroupRepository fileGroupRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting Data Migration for File Versioning...");

        List<FileEntity> unmigratedFiles = fileRepository.findAll().stream()
                .filter(file -> file.getFileGroup() == null)
                .toList();

        if (unmigratedFiles.isEmpty()) {
            logger.info("No files need migration. Versioning schema is up-to-date.");
            return;
        }

        logger.info("Found {} files to migrate to FileGroups", unmigratedFiles.size());

        for (FileEntity file : unmigratedFiles) {
            // Create a new FileGroup for this legacy file
            FileGroupEntity fileGroup = FileGroupEntity.builder()
                    .owner(file.getOwner())
                    .displayFileName(file.getFileName())
                    .currentVersionId(file.getId())
                    .build();

            fileGroup = fileGroupRepository.save(fileGroup);

            // Update the legacy file to belong to this group as version 1
            file.setFileGroup(fileGroup);
            file.setVersionNumber(1);
            file.setIsCurrentVersion(true);
            
            fileRepository.save(file);
            logger.info("Migrated file '{}' (ID: {}) to new FileGroup (ID: {})", file.getFileName(), file.getId(), fileGroup.getId());
        }

        logger.info("Data Migration completed successfully.");
    }
}
