package com.dsevSport.DSEV_Sport.commerce.model;

import com.dsevSport.DSEV_Sport.common.util.enums.CouponDiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity @Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true, nullable = false, length = 50)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    CouponDiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    BigDecimal discountValue;

    @Column(name = "max_uses")
    @Builder.Default
    Integer maxUses = 1;

    @Column(name = "current_uses")
    @Builder.Default
    Integer currentUses = 0;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isValid() {
        if (!isActive) return false;
        if (currentUses >= maxUses) return false;
        return expiresAt == null || !LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementUsage() {
        this.currentUses++;
    }
}
