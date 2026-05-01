package com.smartshare.repository;

import com.smartshare.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByFirebaseUid(String firebaseUid);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as count FROM users WHERE created_at >= CURRENT_DATE - INTERVAL '7 days' GROUP BY CAST(created_at AS DATE) ORDER BY date", nativeQuery = true)
    List<Object[]> findNewUsersPerDayLast7Days();
}
