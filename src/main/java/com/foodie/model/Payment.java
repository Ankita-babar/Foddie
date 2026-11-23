package com.foodie.model;

import java.math.BigDecimal;

public record Payment(
        Integer paymentId,
        Integer orderId,
        String paymentMethod,
        String paymentStatus,
        BigDecimal amount) {
}
