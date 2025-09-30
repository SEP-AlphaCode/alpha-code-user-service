# Stage 1: Build với Maven (không dùng alpine)
FROM maven:3.9-eclipse-temurin-24 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run ứng dụng
FROM eclipse-temurin:24-jdk AS runtime

WORKDIR /app

COPY --from=build /app/target/alpha-code-user-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "app.jar"]