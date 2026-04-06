package com.ivan.inbound.dto;

public record OrderDto (
    String name,
    int quantity,
    double price
) { }
