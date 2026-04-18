package com.ivan.inbound.dto;

import com.ivan.inbound.validator.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderDto(
        @NotNull(message = "cannot be null")
        Long customerId,

        @NotNull(message = "cannot be null")
        Long productId,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String name,

        @NotNull(message = "cannot be null")
        @Positive(message = "must be positive")
        Integer quantity,

        @NotNull(message = "cannot be null")
        @Positive(message = "must be positive")
        Double price,

        @ValidCurrency
        String currency,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String type,

        @NotBlank(message = "cannot be blank")
        @NotNull(message = "cannot be null")
        String shippingAddress,

        @NotNull(message = "cannot be null")
        Boolean firstPurchase
) {}
