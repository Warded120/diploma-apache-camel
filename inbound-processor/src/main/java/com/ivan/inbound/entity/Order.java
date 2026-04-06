package com.ivan.inbound.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Order {
    private String name;
    private int quantity;
    private double price;
}
