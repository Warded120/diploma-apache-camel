package com.ivan.outbound.repository;

import com.ivan.outbound.entity.Order;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class OrderRepo {
    private final EntityManagerFactory emf;

    public Order findById(Long id) {
        try (var em = emf.createEntityManager()) {
            return Optional.ofNullable(id)
                    .map(oid -> em.find(Order.class, oid))
                    .orElseThrow(() -> new EntityNotFoundException("Order with id %s does not exist".formatted(id)));
        }
    }
}

