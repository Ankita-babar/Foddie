package com.foodie.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Integer orderId,
        @NotBlank String paymentMethod,
        @NotBlank String paymentStatus) {
}
