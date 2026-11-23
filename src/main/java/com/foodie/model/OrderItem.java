package com.foodie.model;

import java.math.BigDecimal;

public record OrderItem(
        Integer orderItemId,
        Integer orderId,
        Integer foodId,
        String foodName,
        Integer quantity,
        BigDecimal unitPrice) {
}
