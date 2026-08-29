package com.example.demo.repo.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.models.payments.WebhookLog;

import java.util.List;
import java.util.Optional;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {

    Optional<WebhookLog> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    boolean existsByEventIdAndStatus(String eventId, WebhookLog.WebhookStatus status);

    List<WebhookLog> findByStatus(WebhookLog.WebhookStatus status);

    @Query("SELECT w FROM WebhookLog w WHERE w.status = :status AND w.retryCount < :maxRetries ORDER BY w.createdAt ASC")
    List<WebhookLog> findFailedWebhooksForRetry(
            @Param("status") WebhookLog.WebhookStatus status, 
            @Param("maxRetries") Integer maxRetries
    );
}