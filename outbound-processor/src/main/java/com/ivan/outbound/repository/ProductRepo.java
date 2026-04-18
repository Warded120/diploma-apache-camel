package com.ivan.outbound.repository;

import com.ivan.outbound.entity.Product;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
public class ProductRepo {
    private final EntityManagerFactory emf;

    public Product findById(Long id) {
        try (var em = emf.createEntityManager()) {
            return Optional.ofNullable(id)
                    .map(pid -> em.find(Product.class, pid))
                    .orElseThrow(() -> new EntityNotFoundException("No Product found with id: %s".formatted(id)));
        }
    }
}
