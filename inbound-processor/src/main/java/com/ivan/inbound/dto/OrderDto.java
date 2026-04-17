package com.ivan.inbound.dto;

import com.ivan.inbound.validator.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderDto(
        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String customerId,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String productId,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String name,

        @Positive(message = "must be positive")
        int quantity,

        @Positive(message = "must be positive")
        double price,

        @ValidCurrency
        String currency,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String type,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String shippingAddress,

        boolean firstPurchase
) {}
