package com.ivan.outbound.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.component.jpa.JpaComponent;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JpaConfig {
    @BindToRegistry("entityManagerFactory")
    public EntityManagerFactory createEntityManagerFactory() {
        return Persistence.createEntityManagerFactory("camel");
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
}
