package com.foodie.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Integer orderId,
        @NotNull Integer foodId,
        @NotNull @Min(1) Integer quantity) {
}
