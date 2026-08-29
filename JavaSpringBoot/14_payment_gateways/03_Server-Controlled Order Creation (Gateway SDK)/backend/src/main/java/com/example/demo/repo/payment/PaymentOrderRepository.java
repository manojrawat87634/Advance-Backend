package com.example.demo.repo.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.models.payments.PaymentOrder;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderReferenceId(String orderReferenceId);

    Optional<PaymentOrder> findByGatewayOrderId(String gatewayOrderId);

    List<PaymentOrder> findByUserIdAndStatus(Long userId, PaymentOrder.PaymentStatus status);

    List<PaymentOrder> findByUserId(Long userId);

    @Query("SELECT p FROM PaymentOrder p WHERE p.status = 'PENDING' AND p.createdAt < :cutoff")
    List<PaymentOrder> findExpiredOrders(@Param("cutoff") java.time.LocalDateTime cutoff);
}