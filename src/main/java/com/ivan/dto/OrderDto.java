package com.ivan.dto;

public record OrderDto (
    String name,
    int quantity,
    double price
) { }
