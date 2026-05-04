package com.cdweb.bookstore.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        Integer quantity
) {}