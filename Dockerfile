# Use Maven to build the project and then use OpenJDK for runtime
FROM maven:3.9.7-amazoncorretto-21-al2023 AS build
WORKDIR /app
COPY pom.xml .
COPY inbound-processor/pom.xml inbound-processor/pom.xml
COPY outbound-processor/pom.xml outbound-processor/pom.xml
COPY inbound-processor/src inbound-processor/src
COPY outbound-processor/src outbound-processor/src
RUN mvn -B clean package -DskipTests

# Runtime image for inbound-processor
FROM amazoncorretto:21.0.10-alpine AS inbound-processor
WORKDIR /app
COPY --from=build /app/inbound-processor/target/inbound-processor-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime image for outbound-processor
FROM amazoncorretto:21.0.10-alpine AS outbound-processor
WORKDIR /app
COPY --from=build /app/outbound-processor/target/outbound-processor-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

