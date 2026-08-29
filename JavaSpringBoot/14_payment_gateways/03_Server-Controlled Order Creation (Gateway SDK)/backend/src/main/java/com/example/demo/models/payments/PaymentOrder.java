package com.example.demo.models.payments;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "payment_orders",
    indexes = {
        @Index(name = "idx_user_orders", columnList = "user_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_reference_id", nullable = false, unique = true, length = 64)
    private String orderReferenceId;

    @Builder.Default
    @Column(name = "gateway_name", nullable = false, length = 30)
    private String gatewayName = "RAZORPAY";

    @Column(name = "gateway_order_id", unique = true, length = 255)
    private String gatewayOrderId;

    @Column(name = "amount_in_subunits", nullable = false)
    private Long amountInSubunits;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        CANCELLED,
        EXPIRED
    }
}