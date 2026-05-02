package com.cdweb.bookstore.modules.order.dto;

import java.math.BigDecimal;

/**
 * Kết quả validate mã giảm giá (dùng cho cả preview và internal checkout).
 * isValid = false → errorMessage mô tả lý do.
 * isValid = true  → discountAmount là số tiền được giảm thực tế.
 */
public record CouponValidationResponse(
        boolean isValid,
        String couponCode,
        String couponType,     // PERCENTAGE / FIXED_AMOUNT
        BigDecimal discountAmount,
        String errorMessage
) {
    public static CouponValidationResponse valid(String code, String type, BigDecimal discountAmount) {
        return new CouponValidationResponse(true, code, type, discountAmount, null);
    }

    public static CouponValidationResponse invalid(String code, String reason) {
        return new CouponValidationResponse(false, code, null, BigDecimal.ZERO, reason);
    }
}