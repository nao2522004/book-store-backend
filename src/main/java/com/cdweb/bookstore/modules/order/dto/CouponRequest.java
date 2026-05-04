package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Coupon;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(

        @NotBlank(message = "Mã coupon không được để trống")
        @Size(min = 3, max = 50, message = "Mã coupon phải từ 3 đến 50 ký tự")
        String code,

        @NotNull(message = "Loại coupon không được để trống")
        Coupon.CouponType type,

        @NotNull(message = "Giá trị giảm không được để trống")
        @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
        BigDecimal value,

        // Đơn hàng tối thiểu để áp dụng (null = không giới hạn)
        @DecimalMin(value = "0", message = "Giá trị tối thiểu không được âm")
        BigDecimal minOrderAmount,

        // Giới hạn số tiền giảm tối đa (null = không giới hạn, thường dùng với PERCENTAGE)
        @DecimalMin(value = "0", message = "Giảm tối đa không được âm")
        BigDecimal maxDiscountAmount,

        // Số lần sử dụng tối đa (null = không giới hạn)
        @Min(value = 1, message = "Giới hạn sử dụng phải ít nhất 1")
        Integer usageLimit,

        Instant startDate,
        Instant endDate,

        @NotNull(message = "Trạng thái không được để trống")
        Coupon.CouponStatus status
) {}