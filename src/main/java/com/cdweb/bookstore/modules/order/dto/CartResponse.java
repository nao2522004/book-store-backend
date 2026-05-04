package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        int totalItems,
        BigDecimal totalAmount,
        List<CartItemResponse> items
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        return new CartResponse(
                cart.getId(),
                cart.getTotalItems(),
                cart.getTotalAmount(),
                items
        );
    }
}