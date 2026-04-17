
package com.ivan.outbound.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String productId;

    private String name;
    private String category;
    private String brand;
    private double basePrice;
}