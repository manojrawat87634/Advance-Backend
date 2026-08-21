package com.example.demo.repo.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.models.auth.UserSessionModel;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSessionModel, Long> {
  Optional<UserSessionModel> findByRefreshToken(String refreshToken);

  Optional<UserSessionModel> findBySessionId(Long sessionId);

  boolean existsBySessionIdAndIsRevokedFalse(String sessionId);

  @Modifying
  @Query("UPDATE UserSessionModel s SET s.isRevoked = true, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.isRevoked = false")
  int revokeAllSessionsByUserId(@Param("userId") Long userId);
  
}