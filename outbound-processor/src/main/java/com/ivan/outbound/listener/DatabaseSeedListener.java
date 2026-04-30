package com.ivan.outbound.listener;

import com.ivan.outbound.entity.Customer;
import com.ivan.outbound.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.MainListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSeedListener extends MainListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeedListener.class);

    private static final String CUSTOMERS_CSV = "seed/customers.csv";
    private static final String PRODUCTS_CSV  = "seed/products.csv";

    @Override
    public void afterStart(BaseMainSupport main) {
        log.info("DatabaseSeedListener: checking whether seed data is needed…");

        EntityManagerFactory emf = main.getCamelContext()
                .getRegistry()
                .lookupByNameAndType("entityManagerFactory", EntityManagerFactory.class);

        if (emf == null) {
            log.error("DatabaseSeedListener: EntityManagerFactory not found in registry – skipping seed.");
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            long customerCount = em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class).getSingleResult();
            long productCount  = em.createQuery("SELECT COUNT(p) FROM Product p",  Long.class).getSingleResult();

            if (customerCount == 0) {
                List<Customer> customers = loadCustomers();
                customers.forEach(em::persist);
                log.info("DatabaseSeedListener: seeded {} customers.", customers.size());
            } else {
                log.info("DatabaseSeedListener: customers table already has {} rows – skipping.", customerCount);
            }

            if (productCount == 0) {
                List<Product> products = loadProducts();
                products.forEach(em::persist);
                log.info("DatabaseSeedListener: seeded {} products.", products.size());
            } else {
                log.info("DatabaseSeedListener: products table already has {} rows – skipping.", productCount);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("DatabaseSeedListener: seed failed – rolling back.", e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }

    private List<Customer> loadCustomers() throws Exception {
        List<Customer> list = new ArrayList<>();
        try (BufferedReader reader = openCsv(CUSTOMERS_CSV)) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // skip header row
                String[] cols = splitCsvLine(line);
                if (cols.length < 5) continue;

                Customer c = new Customer();
                c.setFirstName(cols[0].trim());
                c.setLastName(cols[1].trim());
                c.setEmail(cols[2].trim());
                c.setPhone(cols[3].trim());
                c.setShippingAddress(cols[4].trim());
                list.add(c);
            }
        }
        return list;
    }

    private List<Product> loadProducts() throws Exception {
        List<Product> list = new ArrayList<>();
        try (BufferedReader reader = openCsv(PRODUCTS_CSV)) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // skip header row
                String[] cols = splitCsvLine(line);
                if (cols.length < 3) continue;

                Product p = new Product();
                p.setName(cols[0].trim());
                p.setCategory(cols[1].trim());
                p.setBrand(cols[2].trim());
                list.add(p);
            }
        }
        return list;
    }

    private BufferedReader openCsv(String resourcePath) {
        InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("Seed resource not found on classpath: " + resourcePath);
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}

