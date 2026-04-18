package com.ivan.outbound.repository;

import com.ivan.outbound.entity.Customer;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomerRepo {
    private final EntityManagerFactory emf;

    public Customer findById(Long id) {
        try (var em = emf.createEntityManager()) {
            return Optional.ofNullable(id)
                    .map(cid -> em.find(Customer.class, cid))
                    .orElseThrow(() -> new EntityNotFoundException("No Customer found with id: %s".formatted(id)));
        }
    }
}
