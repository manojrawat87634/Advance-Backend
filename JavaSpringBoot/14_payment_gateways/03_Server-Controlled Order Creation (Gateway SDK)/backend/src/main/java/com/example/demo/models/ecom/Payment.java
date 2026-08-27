package com.example.demo.models.ecom;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.ecom.enums.*;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user", columnList = "user_id, status"),
    @Index(name = "idx_payments_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Direct relationship to Order
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_order"))
    private OrderModel order;

    // Direct relationship to User
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_user"))
    private UserModel user;

    @Column(nullable = false)
    private Long amount; // In PAISE

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Column(length = 50)
    private String method; // e.g., 'UPI', 'CARD', 'NETBANKING'

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "razorpay_payment_id", nullable = false, unique = true, length = 255)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", nullable = false, length = 255)
    private String razorpaySignature;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}