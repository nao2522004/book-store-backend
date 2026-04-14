package com.cdweb.bookstore.modules.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// ─── Coupon ───────────────────────────────────────────────────────────────────
@Entity
@Table(name = "coupons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;   // PERCENTAGE, FIXED_AMOUNT

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;   // ACTIVE, INACTIVE, EXPIRED

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "coupon")
    @Builder.Default
    private List<CouponUsage> usages = new ArrayList<>();

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public boolean isValid(BigDecimal orderAmount) {
        Instant today = Instant.now();
        return status == CouponStatus.ACTIVE
                && (startDate == null || !today.isBefore(startDate))
                && (endDate == null || !today.isAfter(endDate))
                && (usageLimit == null || usedCount < usageLimit)
                && (minOrderAmount == null || orderAmount.compareTo(minOrderAmount) >= 0);
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        BigDecimal discount = type == CouponType.PERCENTAGE
                ? orderAmount.multiply(value).divide(BigDecimal.valueOf(100))
                : value;
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0)
            discount = maxDiscountAmount;
        return discount.min(orderAmount);
    }

    public enum CouponType {PERCENTAGE, FIXED_AMOUNT}

    public enum CouponStatus {ACTIVE, INACTIVE, EXPIRED}
}