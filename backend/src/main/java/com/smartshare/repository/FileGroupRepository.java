package com.smartshare.repository;

import com.smartshare.model.entity.FileGroupEntity;
import com.smartshare.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileGroupRepository extends JpaRepository<FileGroupEntity, UUID> {
    Optional<FileGroupEntity> findFirstByOwnerAndDisplayFileNameOrderByCreatedAtDesc(UserEntity owner, String displayFileName);
}
