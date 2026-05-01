package com.smartshare.repository.tag;

import com.smartshare.model.entity.tag.TagEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {
    List<TagEntity> findByFileHash(String fileHash);
    List<TagEntity> findByTag(String tag);
    List<TagEntity> findByFileHashIn(List<String> fileHashes);

    @Query("SELECT t.tag, COUNT(t) FROM TagEntity t GROUP BY t.tag ORDER BY COUNT(t) DESC")
    List<Object[]> findPopularTags(Pageable pageable);

    @Query("SELECT t.tag, COUNT(t) FROM TagEntity t WHERE t.fileHash IN (SELECT f.fileHash FROM FileEntity f WHERE f.owner.firebaseUid = :firebaseUid) GROUP BY t.tag ORDER BY COUNT(t) DESC")
    List<Object[]> findPopularTagsByUser(@org.springframework.data.repository.query.Param("firebaseUid") String firebaseUid, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM TagEntity t WHERE t.fileHash = :fileHash")
    void deleteByFileHash(@org.springframework.data.repository.query.Param("fileHash") String fileHash);
}
