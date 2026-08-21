package com.example.demo.models.ecom.orders;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.ecom.items.Item;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_user_status", columnList = "user_id, status"),
        @Index(name = "idx_orders_razorpay_id", columnList = "razorpay_order_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Direct Foreign Key Column Mapping for direct queries without loading full User entity
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Direct Foreign Key Column Mapping
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    // Entity Relationships (Lazy loaded for optimal performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @Column(name = "amount", nullable = false)
    private Long amount; // Total amount in PAISE

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('CREATED', 'PAID', 'FAILED', 'EXPIRED') DEFAULT 'CREATED'")
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "razorpay_order_id", nullable = false, unique = true)
    private String razorpayOrderId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Inner Enum matching SQL ENUM options
    public enum OrderStatus {
        CREATED,
        PAID,
        FAILED,
        EXPIRED
    }
}