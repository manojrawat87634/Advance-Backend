package com.example.demo.apiKey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.apiKey.entity.ApiKey;
import com.example.demo.models.auth.UserModel;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    // Optional<ApiKey> findByApiKey(String apiKey);
    @Query("""
    SELECT a
    FROM ApiKey a
    JOIN FETCH a.user
    WHERE a.apiKey = :apiKey
""")
Optional<ApiKey> findByApiKey(@Param("apiKey") String apiKey);
    List<ApiKey> findByUser(UserModel user);
    List<ApiKey> findByUserAndActiveTrue(UserModel user);
    boolean existsByApiKey(String apiKey);
}