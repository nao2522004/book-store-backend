package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Order;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
        @NotNull(message = "Trạng thái thanh toán không được để trống")
        Order.PaymentStatus paymentStatus
) {}