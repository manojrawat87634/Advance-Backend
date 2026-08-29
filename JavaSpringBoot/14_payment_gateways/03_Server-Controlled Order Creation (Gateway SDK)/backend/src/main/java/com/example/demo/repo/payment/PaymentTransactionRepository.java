package com.example.demo.repo.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.payments.PaymentTransaction;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByGatewayPaymentId(String gatewayPaymentId);

    List<PaymentTransaction> findByPaymentOrderId(Long paymentOrderId);

    List<PaymentTransaction> findByPaymentOrderIdAndStatus(Long paymentOrderId, PaymentTransaction.TransactionStatus status);

    boolean existsByGatewayPaymentIdAndStatus(String gatewayPaymentId, PaymentTransaction.TransactionStatus status);
}