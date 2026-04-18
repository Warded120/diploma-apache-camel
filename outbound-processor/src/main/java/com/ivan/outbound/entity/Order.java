package com.ivan.outbound.entity;

import com.ivan.outbound.enumeration.OrderType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import static com.ivan.outbound.constants.JpaConstants.DELETE_BY_ID;

@Entity
@Getter
@Setter
@Table(name = "orders")
@NamedQuery(name = DELETE_BY_ID, query = "delete from Order o where o.id = :id")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String name;
    private Integer quantity;

    private Double priceUsd; //TODO: add currency api

    @Enumerated(EnumType.STRING)
    private OrderType type;

    private String shippingAddress;
    private Boolean firstPurchaseDiscountApplied;
}
