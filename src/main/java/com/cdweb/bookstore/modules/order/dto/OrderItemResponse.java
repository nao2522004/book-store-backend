package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long bookId,
        String bookTitleSnapshot,
        String bookCoverSnapshot,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static OrderItemResponse fromOrderItem(OrderItem item) {
        return new OrderItemResponse(
                item.getBook().getId(),
                item.getBookTitleSnapshot(),
                item.getBookCoverSnapshot(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}