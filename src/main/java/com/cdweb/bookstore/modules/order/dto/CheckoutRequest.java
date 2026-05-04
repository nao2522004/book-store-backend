package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Order;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(

        @NotNull(message = "Địa chỉ giao hàng không được để trống")
        Long addressId,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        Order.PaymentMethod paymentMethod,

        // null = không dùng mã giảm giá
        String couponCode,

        String note
) {}