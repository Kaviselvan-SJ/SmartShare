package com.smartshare.repository.analytics;

import com.smartshare.model.entity.analytics.DownloadAnalyticsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DownloadAnalyticsRepository extends JpaRepository<DownloadAnalyticsEntity, UUID> {
    List<DownloadAnalyticsEntity> findByShortCode(String shortCode);
    long countByShortCode(String shortCode);

    @Query("SELECT d.fileHash, COUNT(d) FROM DownloadAnalyticsEntity d GROUP BY d.fileHash ORDER BY COUNT(d) DESC")
    List<Object[]> findTopDownloadedFiles(Pageable pageable);

    @Query("SELECT d FROM DownloadAnalyticsEntity d ORDER BY d.createdAt DESC")
    List<DownloadAnalyticsEntity> findRecentActivity(Pageable pageable);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as count FROM download_analytics WHERE created_at >= CURRENT_DATE - INTERVAL '7 days' GROUP BY CAST(created_at AS DATE) ORDER BY date", nativeQuery = true)
    List<Object[]> findDownloadsPerDayLast7Days();

    @Query(value = "SELECT COUNT(DISTINCT ip_address) FROM download_analytics WHERE created_at >= NOW() - INTERVAL '24 hours'", nativeQuery = true)
    long countActiveIPsLast24Hours();

    @Query("SELECT COUNT(d) FROM DownloadAnalyticsEntity d WHERE d.fileHash IN (SELECT f.fileHash FROM FileEntity f WHERE f.owner.firebaseUid = :firebaseUid)")
    long countByUserFiles(@org.springframework.data.repository.query.Param("firebaseUid") String firebaseUid);

    @Query("SELECT d.fileHash, COUNT(d) FROM DownloadAnalyticsEntity d WHERE d.fileHash IN (SELECT f.fileHash FROM FileEntity f WHERE f.owner.firebaseUid = :firebaseUid) GROUP BY d.fileHash ORDER BY COUNT(d) DESC")
    List<Object[]> findTopDownloadedFilesByUser(@org.springframework.data.repository.query.Param("firebaseUid") String firebaseUid, Pageable pageable);

    @Query("SELECT d FROM DownloadAnalyticsEntity d WHERE d.fileHash IN (SELECT f.fileHash FROM FileEntity f WHERE f.owner.firebaseUid = :firebaseUid) ORDER BY d.createdAt DESC")
    List<DownloadAnalyticsEntity> findRecentActivityByUser(@org.springframework.data.repository.query.Param("firebaseUid") String firebaseUid, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM DownloadAnalyticsEntity d WHERE d.fileHash = :fileHash")
    void deleteByFileHash(@org.springframework.data.repository.query.Param("fileHash") String fileHash);
    @Query("SELECT d FROM DownloadAnalyticsEntity d WHERE d.fileHash = :fileHash ORDER BY d.createdAt DESC")
    List<DownloadAnalyticsEntity> findRecentActivityByFileHash(@org.springframework.data.repository.query.Param("fileHash") String fileHash, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM DownloadAnalyticsEntity d WHERE d.shortCode = :shortCode")
    void deleteByShortCode(@org.springframework.data.repository.query.Param("shortCode") String shortCode);
}
