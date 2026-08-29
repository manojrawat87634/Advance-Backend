package com.example.demo.models.payments;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_entitlements",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_media", columnNames = {"user_id", "media_asset_id"})
    },
    indexes = {
        @Index(name = "idx_user_media_active", columnList = "user_id, media_asset_id, is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "media_asset_id", nullable = false)
    private Long mediaAssetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_transaction_id", foreignKey = @ForeignKey(name = "fk_entitlement_transaction"))
    private PaymentTransaction grantedByTransaction;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}