package com.foodie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FoodOrder(
        Integer orderId,
        Integer customerId,
        String customerName,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount) {
}
