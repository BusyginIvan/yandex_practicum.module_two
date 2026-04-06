# ===== build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY openapi openapi
COPY market-app market-app
COPY payment-service payment-service
RUN mvn -q -DskipTests -pl payment-service -am clean package

# ===== runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
