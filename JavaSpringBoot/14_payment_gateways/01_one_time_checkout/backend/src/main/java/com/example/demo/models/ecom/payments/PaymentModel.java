package com.example.demo.models.ecom.payments;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.models.ecom.orders.OrderModel;
import com.example.demo.models.auth.UserModel;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_user", columnList = "user_id, status"),
        @Index(name = "idx_payments_order", columnList = "order_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Direct Foreign Key Column Mapping
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    // Direct Foreign Key Column Mapping
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Entity Relationships (Lazy loaded for optimal performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderModel order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserModel user;

    @Column(name = "amount", nullable = false)
    private Long amount; // Amount paid in PAISE

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "method", length = 50)
    private String method; // e.g., 'card', 'upi', 'netbanking'

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('SUCCESS', 'FAILED', 'PENDING', 'REFUNDED') DEFAULT 'PENDING'")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "razorpay_payment_id", nullable = false, unique = true)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", nullable = false)
    private String razorpaySignature;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Inner Enum matching SQL ENUM options
    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING,
        REFUNDED
    }
}