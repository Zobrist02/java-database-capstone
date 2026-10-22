# Build the app with Maven and Java 24
FROM maven:3.9.9-eclipse-temurin-24 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Run the app with Java 24
FROM eclipse-temurin:24-jre

WORKDIR /app

COPY --from=builder /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]