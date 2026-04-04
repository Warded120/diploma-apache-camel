package com.ivan.inbound.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private String name;
    private int quantity;
    private double price;
}
