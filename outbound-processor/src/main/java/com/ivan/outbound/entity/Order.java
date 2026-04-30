package com.ivan.outbound.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price_usd")
    private Double priceUsd;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "first_purchase")
    private Boolean firstPurchase;
}
