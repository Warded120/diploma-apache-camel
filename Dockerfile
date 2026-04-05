# Use Maven to build the project and then use OpenJDK for runtime
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY inbound-processor/pom.xml inbound-processor/pom.xml
COPY outbound-processor/pom.xml outbound-processor/pom.xml
COPY inbound-processor/src inbound-processor/src
COPY outbound-processor/src outbound-processor/src
RUN mvn -B clean package -DskipTests

# Runtime image for inbound-processor
FROM eclipse-temurin:21-jre AS inbound-processor
WORKDIR /app
COPY --from=build /app/inbound-processor/target/inbound-processor-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime image for outbound-processor
FROM eclipse-temurin:21-jre AS outbound-processor
WORKDIR /app
COPY --from=build /app/outbound-processor/target/outbound-processor-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

