package com.ivan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import static com.ivan.constants.JpaConstants.DELETE_BY_ID;
import static com.ivan.constants.JpaConstants.FIND_ALL;
import static com.ivan.constants.JpaConstants.FIND_BY_ID;

@Entity
@Getter
@Setter
@Table(name = "orders")
@NamedQuery(name = FIND_BY_ID, query = "select o from Order o where o.id = :id")
@NamedQuery(name = FIND_ALL, query = "select o from Order o")
@NamedQuery(name = DELETE_BY_ID, query = "delete from Order o where o.id = :id")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private double price;
}
