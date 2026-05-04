package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Coupon;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponResponse(
        Long id,
        String code,
        Coupon.CouponType type,
        BigDecimal value,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        Integer usedCount,
        Instant startDate,
        Instant endDate,
        Coupon.CouponStatus status,
        Instant createdAt
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getType(),
                coupon.getValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                coupon.getUsageLimit(),
                coupon.getUsedCount(),
                coupon.getStartDate(),
                coupon.getEndDate(),
                coupon.getStatus(),
                coupon.getCreatedAt()
        );
    }
}