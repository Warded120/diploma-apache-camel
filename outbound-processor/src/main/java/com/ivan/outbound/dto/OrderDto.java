package com.ivan.outbound.dto;

public record OrderDto (
    String name,
    int quantity,
    double price
) { }
