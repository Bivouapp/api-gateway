# --- Build stage for Spring Boot application ---
FROM gradle:8-jdk23 AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and settings files
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Copy the application source code
COPY . .

# Build the application
RUN ./gradlew clean build --no-daemon

# --- Runtime stage ---
FROM eclipse-temurin:23-jdk-alpine

# Set the working directory for the application
WORKDIR /app

# Copy the built application jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8081

# Start the application
CMD ["java", "-jar", "app.jar"]
