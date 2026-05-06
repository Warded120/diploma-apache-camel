package com.ivan.outbound.config;

import com.ivan.outbound.processor.ResolveCustomerProcessor;
import com.ivan.outbound.processor.ResolveProductProcessor;
import com.ivan.outbound.processor.ValidateOrderExistsProcessor;
import com.ivan.outbound.repository.CustomerRepo;
import com.ivan.outbound.repository.OrderRepo;
import com.ivan.outbound.repository.ProductRepo;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.component.jpa.JpaComponent;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JpaConfig {
    @BindToRegistry("entityManagerFactory")
    public EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();

        // Override persistence.xml properties with environment variables if present
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbHost != null && dbPort != null && dbName != null) {
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
            properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
        }

        if (dbUser != null) {
            properties.put("jakarta.persistence.jdbc.user", dbUser);
        }

        if (dbPassword != null) {
            properties.put("jakarta.persistence.jdbc.password", dbPassword);
        }

        // Create EntityManagerFactory with overridden properties
        return properties.isEmpty()
            ? Persistence.createEntityManagerFactory("camel")
            : Persistence.createEntityManagerFactory("camel", properties);
    }

    @BindToRegistry("tm")
    public PlatformTransactionManager createTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @BindToRegistry("jpa")
    public JpaComponent createJpaComponent(
            EntityManagerFactory entityManagerFactory
    ) {
        JpaComponent jpaComponent = new JpaComponent();
        jpaComponent.setEntityManagerFactory(entityManagerFactory);
        jpaComponent.setSharedEntityManager(true);
        jpaComponent.setJoinTransaction(true);
        return jpaComponent;
    }

    @BindToRegistry("customerRepo")
    public CustomerRepo createCustomerRepo(EntityManagerFactory entityManagerFactory) {
        return new CustomerRepo(entityManagerFactory);
    }

    @BindToRegistry("productRepo")
    public ProductRepo createProductRepo(EntityManagerFactory entityManagerFactory) {
        return new ProductRepo(entityManagerFactory);
    }

    @BindToRegistry("orderRepo")
    public OrderRepo createOrderRepo(EntityManagerFactory entityManagerFactory) {
        return new OrderRepo(entityManagerFactory);
    }

    @BindToRegistry("resolveCustomerProcessor")
    public ResolveCustomerProcessor createResolveCustomerProcessor(CustomerRepo customerRepo) {
        return new ResolveCustomerProcessor(customerRepo);
    }

    @BindToRegistry("resolveProductProcessor")
    public ResolveProductProcessor createResolveProductProcessor(ProductRepo productRepo) {
        return new ResolveProductProcessor(productRepo);
    }

    @BindToRegistry("validateOrderExistsProcessor")
    public ValidateOrderExistsProcessor createValidateOrderExistsProcessor(OrderRepo orderRepo) {
        return new ValidateOrderExistsProcessor(orderRepo);
    }
}
