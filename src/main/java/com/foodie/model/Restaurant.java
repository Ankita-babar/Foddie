package com.foodie.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Restaurant(
        Integer restaurantId,
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank @Size(min = 8, max = 15) String phone,
        @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating) {
}
