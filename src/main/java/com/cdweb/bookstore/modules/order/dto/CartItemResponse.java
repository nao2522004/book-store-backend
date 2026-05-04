package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,
        Long bookId,
        String bookTitle,
        String bookCoverUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static CartItemResponse from(CartItem item) {
        BigDecimal unitPrice = item.getBook().getEffectivePrice();
        return new CartItemResponse(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getCoverUrl(),
                unitPrice,
                item.getQuantity(),
                unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}