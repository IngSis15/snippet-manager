# Stage 1: Build the application
FROM gradle:8.1-jdk17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the gradle wrapper and project files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy the source files
COPY src src

# Build the project
RUN ./gradlew build --no-daemon

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
