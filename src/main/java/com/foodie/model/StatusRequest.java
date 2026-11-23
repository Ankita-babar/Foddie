package com.foodie.model;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(@NotBlank String status) {
}
