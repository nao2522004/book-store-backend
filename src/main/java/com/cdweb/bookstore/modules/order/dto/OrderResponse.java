package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String recipientName,
        String recipientPhone,
        String shippingAddress,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String couponCode,
        String note,
        Order.OrderStatus status,
        Order.PaymentMethod paymentMethod,
        Order.PaymentStatus paymentStatus,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse fromOrder(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::fromOrderItem)
                .toList();

        String couponCode = order.getCoupon() != null ? order.getCoupon().getCode() : null;

        return new OrderResponse(
                order.getId(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getShippingAddress(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getTotalAmount(),
                couponCode,
                order.getNote(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}