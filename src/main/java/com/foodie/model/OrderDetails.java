package com.foodie.model;

import java.util.List;

public record OrderDetails(FoodOrder order, List<OrderItem> items, Payment payment) {
}
