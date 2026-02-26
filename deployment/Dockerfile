# ---------- Stage 1: Build the application ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory.
WORKDIR /app

# Copy Gradle wrapper and build files.
COPY gradlew settings.gradle build.gradle main.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies || true

# Copy the entire project source.
COPY . .

# Build the application
RUN ./gradlew clean build -x test -x validateStructure

# ---------- Stage 2: Runtime image ----------
FROM eclipse-temurin:21-jre

# Set working directory.
WORKDIR /app

# Copy the built jar from builder stage.
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar

# Run the application.
ENTRYPOINT ["java", "-jar", "app.jar"]