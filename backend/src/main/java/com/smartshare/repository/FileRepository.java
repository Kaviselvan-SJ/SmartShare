package com.smartshare.repository;

import com.smartshare.model.entity.FileEntity;
import com.smartshare.model.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByFileHash(String fileHash);

    List<FileEntity> findByOwnerOrderByCreatedAtDesc(UserEntity owner);

    long countByOwner_FirebaseUid(String firebaseUid);

    @Query("SELECT f.owner, COUNT(f) FROM FileEntity f GROUP BY f.owner ORDER BY COUNT(f) DESC")
    List<Object[]> findTopUsersByUploads(Pageable pageable);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as count FROM files WHERE created_at >= CURRENT_DATE - INTERVAL '7 days' GROUP BY CAST(created_at AS DATE) ORDER BY date", nativeQuery = true)
    List<Object[]> findUploadsPerDayLast7Days();

    @Query(value = "SELECT COUNT(DISTINCT owner_id) FROM files WHERE created_at >= NOW() - INTERVAL '24 hours'", nativeQuery = true)
    long countActiveUsersLast24Hours();

    @Query("SELECT f FROM FileEntity f ORDER BY f.createdAt DESC")
    List<FileEntity> findRecentUploads(Pageable pageable);
}
