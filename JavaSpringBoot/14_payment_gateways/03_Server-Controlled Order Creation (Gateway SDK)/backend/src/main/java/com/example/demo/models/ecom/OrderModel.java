package com.example.demo.models.ecom;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.example.demo.models.auth.UserModel;
import com.example.demo.models.ecom.enums.OrderStatus;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_status", columnList = "user_id, status"),
    @Index(name = "idx_orders_razorpay_id", columnList = "razorpay_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many orders belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_user"))
    private UserModel user;

    // Many orders belong to one item
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_item"))
    private ItemModel item;

    @Column(nullable = false)
    private Long amount; // In PAISE

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "razorpay_order_id", nullable = false, unique = true, length = 255)
    private String razorpayOrderId;

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