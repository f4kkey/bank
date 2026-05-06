FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application
COPY src ./src
RUN mvn package -DskipTests -B \
    && mvn dependency:copy-dependencies -DoutputDirectory=target/libs -B

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/bank-1.0-SNAPSHOT.jar ./app.jar
COPY --from=build /app/target/libs ./libs

EXPOSE 12345

ENTRYPOINT ["java", "-cp", "app.jar:libs/*", "com.khanh.App"]
