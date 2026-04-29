# GitHub Copilot – Project Context

## Overview

**diploma-apache-camel** is a multi-module Maven project (Java 21, Apache Camel 4.14.0 standalone
via `camel-main` — **no Spring Boot**) that demonstrates an event-driven order-processing pipeline.

```
diploma-apache-camel/          ← parent POM (packaging=pom)
├── avro-schema/               ← shared Avro schema + generated Java classes
├── inbound-processor/         ← REST API → Kafka producer
└── outbound-processor/        ← Kafka consumer → enrichment → PostgreSQL
```

GroupId: `com.ivan` | Version: `1.0-SNAPSHOT`

---

## Infrastructure (Docker Compose)

| Service           | Image                                 | Port(s)        | Notes                              |
|-------------------|---------------------------------------|----------------|------------------------------------|
| `kafka`           | `confluentinc/cp-kafka:8.1.0`         | 9092, 9101 JMX | KRaft mode (no ZooKeeper)          |
| `schema-registry` | `confluentinc/cp-schema-registry:8.1.0` | 8181         | Confluent Schema Registry          |
| `postgres`        | `postgres:16.2`                       | 5432           | DB `diploma`, user `diploma`/`password` |

All three services share the `main_network` Docker bridge network.  
Kafka uses KRaft (`KAFKA_PROCESS_ROLES: broker,controller`).  
Schema Registry is reachable at `http://localhost:8181` (env: `SCHEMA_REGISTRY_URL`).

**Quick start:**
```bash
docker compose up -d
mvn install          # builds all modules, runs tests
```

---

## Module: `avro-schema`

**Purpose:** Generates the shared `OrderMessage` Avro class consumed by both processors.

> ⚠️ TODO: rename to `commons` and move other shared classes here (see `avro-schema/pom.xml`).

### Schema – `OrderMessage.avsc`

Namespace: `com.ivan.avro`

| Avro field | Type      | Meaning                   |
|------------|-----------|---------------------------|
| `cid`      | `long`    | customerId                |
| `pid`      | `long`    | productId                 |
| `qty`      | `int`     | quantity                  |
| `pr`       | `double`  | original price            |
| `cur`      | `string`  | original currency code    |
| `addr`     | `string`  | shippingAddress           |
| `fp`       | `boolean` | firstPurchase flag        |

Generated class lives at `com.ivan.avro.OrderMessage` (produced by `avro-maven-plugin` into
`target/generated-sources/avro`).

---

## Module: `inbound-processor`

**Entry point:** `com.ivan.inbound.InboundProcessorApplication`  
**Configuration:** `src/main/resources/application.properties`

### Responsibilities
1. Exposes a REST API (Camel REST DSL + `camel-netty-http`, port **8080**).
2. Validates incoming JSON with Jakarta Bean Validation.
3. Maps the validated DTO → `OrderMessage` (MapStruct).
4. Serialises and publishes to Kafka using the **Confluent Avro serializer**.

### REST Endpoints (`/orders`)

| Method   | Path           | Camel Route              |
|----------|----------------|--------------------------|
| `POST`   | `/orders`      | `create-order-route`     |
| `PUT`    | `/orders/{id}` | `update-order-route`     |
| `DELETE` | `/orders/{id}` | `delete-order-route`     |

Defined in `OrdersRoute`. Consumes/produces `application/json`.  
On unhandled exception the message is forwarded to the **dead-letter topic**
(`diploma-orders-topic-dlt`) with an `error` header.

### Request DTO – `OrderDto` (Java record)

```java
record OrderDto(
    @NotNull Long customerId,
    @NotNull Long productId,
    @NotNull @Positive Integer quantity,
    @NotNull @Positive Double price,
    @ValidCurrency String currency,      // custom constraint
    @NotBlank @NotNull String shippingAddress,
    @NotNull Boolean firstPurchase
) {}
```

Validated via `camel-bean-validator` (`beanValidator("orderDtoValidator")`).

### MapStruct Mapper – `OrderMessageMapper`

Maps `OrderDto` → `OrderMessage`:

| DTO field         | Avro field |
|-------------------|------------|
| `customerId`      | `cid`      |
| `productId`       | `pid`      |
| `quantity`        | `qty`      |
| `price`           | `pr`       |
| `currency`        | `cur`      |
| `shippingAddress` | `addr`     |
| `firstPurchase`   | `fp`       |

### Kafka Configuration

```properties
camel.component.kafka.brokers=localhost:9092
camel.component.kafka.value-serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
camel.component.kafka.additional-properties[schema.registry.url]={{env:SCHEMA_REGISTRY_URL:http://localhost:8181}}

kafka.topic=diploma-orders-topic
kafka.dead-letter-topic=diploma-orders-topic-dlt
```

An `action` header is set on every message to identify the operation
(`create` | `update` | `delete`) — constant source: `OrderAction` enum.

### Route Classes

| Class                      | Role                                         |
|----------------------------|----------------------------------------------|
| `OrdersRoute`              | REST DSL entry point, global error handler   |
| `CreateOrderRoute`         | Unmarshal → validate → map → Kafka publish   |
| `UpdateOrderRoute`         | Same as create but with `{id}` path param    |
| `DeleteOrderRoute`         | Extracts `{id}` → Kafka publish              |
| `ValidationErrorHandlerProcessor` | Sets `ValidationError` header on `BeanValidationException` |

---

## Module: `outbound-processor`

**Entry point:** `com.ivan.outbound.OutboundProcessorApplication`  
**Configuration:** `src/main/resources/application.properties`

### Responsibilities
1. Consumes `OrderMessage` from Kafka (Confluent Avro deserialiser).
2. Routes messages by the `action` header to the appropriate sub-route.
3. For **create / update**: resolves `Customer` and `Product` entities from the DB, enriches
   the order with a USD price (via external currency API, JCache-cached), then persists
   the `Order` entity via JPA.
4. For **delete**: runs a named JPQL query to remove the record by id.

### Kafka Configuration

```properties
camel.component.kafka.brokers=localhost:9092
camel.component.kafka.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
camel.component.kafka.additional-properties[schema.registry.url]={{env:SCHEMA_REGISTRY_URL:http://localhost:8181}}
camel.component.kafka.additional-properties[specific.avro.reader]=true

kafka.topic=diploma-orders-topic
kafka.group-id=outbound-processor-group-id
```

### Route Classes

| Class                   | Role                                                               |
|-------------------------|--------------------------------------------------------------------|
| `OrderConsumerRoute`    | Kafka consumer; routes by `action` header to sub-routes           |
| `CreateOrderRoute`      | Resolve customer+product → currency enrich → map → enrich → JPA persist |
| `UpdateOrderRoute`      | Same as create + `SetOrderIdProcessor` sets entity id for merge   |
| `DeleteOrderRoute`      | Named query `DELETE_BY_ID` via `JpaParametersProcessor`           |
| `CurrencyEnricherRoute` | HTTP call to exchange-rate API; JCache result keyed by currency code |

### Create / Update Order Processing Pipeline

```
OrderConsumerRoute (Kafka)
  └─► CreateOrderRoute / UpdateOrderRoute
        ├─ ResolveCustomerProcessor   → sets exchange property PROP_CUSTOMER (Customer entity)
        ├─ ResolveProductProcessor    → sets exchange property PROP_PRODUCT  (Product entity)
        ├─► CurrencyEnricherRoute
        │     ├─ saves OrderMessage to exchange property PROP_ORDER_MESSAGE
        │     ├─ JCachePolicy (key=baseCurrency, TTL=1h, Caffeine backend)
        │     │     └─ GET https://api.exchangerate-api.com/v4/latest/{currency}
        │     │          → unmarshalled to ExchangeRateResponse
        │     └─ CurrencyPriceCalculatorProcessor
        │           ├─ reads USD rate from ExchangeRateResponse.rates
        │           ├─ applies 10% first-purchase discount if fp=true
        │           ├─ sets HEADER_PRICE_USD on exchange
        │           └─ restores OrderMessage as body
        ├─ mapstruct(Order.class)      → OrderMapper maps OrderMessage → Order (qty, addr, fp fields)
        ├─ EnricherProcessor
        │     ├─ order.setCustomer(PROP_CUSTOMER)
        │     ├─ order.setProduct(PROP_PRODUCT)
        │     └─ order.setPriceUsd(HEADER_PRICE_USD)
        └─ jpa(Order.class)            → Hibernate persist/merge
```

### Exchange Headers & Properties

Defined in `ExchangeConstants`:

| Constant              | Type     | Value            | Description                          |
|-----------------------|----------|------------------|--------------------------------------|
| `HEADER_ACTION`       | header   | `"action"`       | Operation type (create/update/delete)|
| `PROP_CUSTOMER`       | property | `"customer"`     | Resolved `Customer` entity           |
| `PROP_PRODUCT`        | property | `"product"`      | Resolved `Product` entity            |
| `PROP_ORDER_MESSAGE`  | property | `"orderMessage"` | Saved `OrderMessage` before HTTP call|
| `HEADER_BASE_CURRENCY`| header   | `"baseCurrency"` | Currency code for enricher route     |
| `HEADER_PRICE_USD`    | header   | `"priceUsd"`     | Computed USD price                   |
| `DISCOUNT`            | constant | `0.1`            | First-purchase discount (10%)        |

### Database & JPA

**persistence.xml** (`META-INF/persistence.xml`, persistence-unit name: `camel`):

```properties
transaction-type="RESOURCE_LOCAL"
provider: org.hibernate.jpa.HibernatePersistenceProvider
jdbc.url: jdbc:postgresql://localhost:5432/diploma
jdbc.user: diploma / password
hibernate.hbm2ddl.auto: create-drop   ← schema recreated on every start
hibernate.show_sql: false
```

**Registered beans** (via `JpaConfig` with `@Configuration` / `@BindToRegistry`):

| Registry name           | Type                       |
|-------------------------|----------------------------|
| `entityManagerFactory`  | `EntityManagerFactory`     |
| `tm`                    | `JpaTransactionManager`    |
| `jpa`                   | `JpaComponent`             |
| `customerRepo`          | `CustomerRepo`             |
| `productRepo`           | `ProductRepo`              |
| `resolveCustomerProcessor` | `ResolveCustomerProcessor` |
| `resolveProductProcessor`  | `ResolveProductProcessor`  |

All write routes use `.transacted()` (backed by the `tm` transaction manager).

### JPA Entities

#### `Order` → table `orders`

| Field                       | Type      | Notes                          |
|-----------------------------|-----------|--------------------------------|
| `id`                        | `Long`    | PK, generated identity         |
| `customer`                  | `Customer`| `@ManyToOne`, `customer_id` FK |
| `product`                   | `Product` | `@ManyToOne`, `product_id` FK  |
| `quantity`                  | `Integer` |                                |
| `priceUsd`                  | `Double`  | Converted & discounted price   |
| `shippingAddress`           | `String`  |                                |
| `firstPurchaseDiscountApplied` | `Boolean` |                             |

Named query `DELETE_BY_ID`: `delete from Order o where o.id = :id`

#### `Customer` → table `customers`

`id`, `firstName`, `lastName`, `email`, `phone`, `defaultShippingAddress`

#### `Product` → table `products`

`id`, `name`, `category`, `brand`

### Database Seeding

`DatabaseSeedListener` (registered as `camel.main.main-listener-classes`) runs on startup
(`afterStart`), checks if `customers` / `products` tables are empty and seeds them from
classpath CSV files:

- `seed/customers.csv` → columns: `firstName, lastName, email, phone, defaultShippingAddress`
- `seed/products.csv`  → columns: `name, category, brand`

---

## Shared Coding Conventions

### Camel DSL
- All routes extend `EndpointRouteBuilder` (Endpoint DSL — type-safe component methods).
- Route names are constants in `RouteConstants`; route IDs follow the pattern `"<name>-id"`.
- `ClassUtil.target(Class)` is a utility that returns the class name string expected by
  `jpa(...)` and `mapstruct(...)` endpoint DSL builders.
- Processors injected via `@BeanInject` must be registered in the Camel registry (either via
  `@BindToRegistry` in a `@Configuration` class or auto-discovered by Camel's classpath scan).

### Dependency Injection (no Spring IoC)
- `@Configuration` on a plain class + `@BindToRegistry` on methods → Camel's own registry.
- `camel-bean` component and `@BeanInject` for route-level field injection.

### MapStruct + Lombok
- Both run as annotation processors in `maven-compiler-plugin`.
- Annotation processor order: **Lombok → MapStruct** (`lombok-mapstruct-binding:0.2.0`).
- Mapper classes are `abstract class` annotated with `@Mapper` (no `componentModel`; Camel
  MapStruct component discovers implementations from the configured package).

### Transaction Handling
- `camel-jpa` + Spring `JpaTransactionManager` for RESOURCE_LOCAL transactions.
- Routes call `.transacted()` to enlist in a transaction.
- `JpaComponent` is configured with `sharedEntityManager=true` and `joinTransaction=true`.

### Logging
- Logback (`logback-classic 1.5.32`); configured via `logback.xml` in each module's resources.
- Camel `.log(...)` steps use Simple language (`${exchange.*}`, `${header.*}`, etc.).

---

## Key Dependencies (root POM)

| Dependency                         | Version         |
|------------------------------------|-----------------|
| Apache Camel BOM                   | 4.14.0          |
| Jackson BOM                        | 2.17.2          |
| Apache Avro                        | 1.11.3          |
| Confluent `kafka-avro-serializer`  | 7.6.0           |
| Hibernate ORM                      | 6.4.0.Final     |
| PostgreSQL JDBC driver             | 42.7.2          |
| MapStruct                          | 1.6.3           |
| Lombok                             | 1.18.42         |
| Logback Classic                    | 1.5.32          |
| Caffeine JCache                    | 3.1.8           |

Additional module-only dependencies:
- `inbound-processor`: `camel-bean-validator`, `avro-schema`
- `outbound-processor`: `camel-jcache`, `camel-http`, `caffeine/jcache`, `avro-schema`

---

## Build & Packaging

- Java 21 (`--release 21`).
- Each application module packages a fat-classpath JAR (`maven-jar-plugin`) and copies all
  runtime dependencies to `target/lib/` (`maven-dependency-plugin`).
- Multi-stage `Dockerfile`: single build stage compiles both modules; two separate runtime
  stages (`inbound-processor` and `outbound-processor`) based on `amazoncorretto:21-alpine`.
- Tests use `camel-test-main-junit5`.

