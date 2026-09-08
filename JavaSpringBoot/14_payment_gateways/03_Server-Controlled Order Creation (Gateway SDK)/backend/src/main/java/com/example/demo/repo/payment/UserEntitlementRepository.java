package com.example.demo.repo.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.models.payments.UserEntitlement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserEntitlementRepository extends JpaRepository<UserEntitlement, Long> {

    Optional<UserEntitlement> findByUserIdAndMediaAssetId(Long userId, Long mediaAssetId);

    List<UserEntitlement> findByUserId(Long userId);

    List<UserEntitlement> findByUserIdAndIsActive(Long userId, Boolean isActive);

    boolean existsByUserIdAndMediaAssetIdAndIsActiveTrue(Long userId, Long mediaAssetId);
    boolean existsByUserIdAndMediaAssetId(Long userId, Long mediaAssetId);
    @Query("SELECT e FROM UserEntitlement e WHERE e.userId = :userId AND e.mediaAssetId = :mediaAssetId " +
           "AND e.isActive = true AND (e.expiresAt IS NULL OR e.expiresAt > :now)")
    Optional<UserEntitlement> findValidEntitlement(
            @Param("userId") Long userId, 
            @Param("mediaAssetId") Long mediaAssetId, 
            @Param("now") LocalDateTime now
    );
}