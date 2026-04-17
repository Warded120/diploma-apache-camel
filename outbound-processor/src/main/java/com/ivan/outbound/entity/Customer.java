package com.ivan.outbound.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private String customerId;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String defaultShippingAddress;
}