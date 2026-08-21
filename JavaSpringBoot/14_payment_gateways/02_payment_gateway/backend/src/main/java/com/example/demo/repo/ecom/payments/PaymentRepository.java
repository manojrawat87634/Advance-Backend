package com.example.demo.repo.ecom.payments;
import com.example.demo.models.ecom.payments.PaymentModel;
import com.example.demo.models.ecom.payments.PaymentModel.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, Long> {
    // 1. Find payment by Razorpay Payment ID (e.g., pay_xxxxx)
    Optional<PaymentModel> findByRazorpayPaymentId(String razorpayPaymentId);

    // 2. Check if payment already exists (prevents double-processing webhooks)
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);

    // 3. Find payments associated with a specific Order (Uses index idx_payments_order)
    List<PaymentModel> findByOrderId(Long orderId);

    // 4. Get the latest successful payment for an Order
    Optional<PaymentModel> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(Long orderId, PaymentStatus status);

    // 5. Find all payments made by a User (Uses index idx_payments_user)
    List<PaymentModel> findByUserId(Long userId);

    // 6. Paginated user payments filtered by status
    Page<PaymentModel> findByUserIdAndStatus(Long userId, PaymentStatus status, Pageable pageable);
}