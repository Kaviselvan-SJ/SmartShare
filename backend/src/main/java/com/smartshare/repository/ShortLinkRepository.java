package com.smartshare.repository;

import com.smartshare.model.entity.ShortLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShortLinkRepository extends JpaRepository<ShortLinkEntity, UUID> {
    Optional<ShortLinkEntity> findByShortCode(String shortCode);
}
