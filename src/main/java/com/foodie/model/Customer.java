package com.foodie.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Customer(
        Integer customerId,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 10, max = 15) String phone,
        @NotBlank String address) {
}
