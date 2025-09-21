# Multi-stage build for Quiz App with OCI PostgreSQL
FROM maven:3.8.5-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy source code
COPY . .

# Build the application with optimizations
RUN cd /app && \
    mvn spotless:apply && \
    mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# Production stage
FROM openjdk:17.0.1-jdk-slim

# Install necessary packages and create non-root user
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    netcat \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built jar file
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Set Spring profile for OCI PostgreSQL
ENV SPRING_PROFILES_ACTIVE=oci-postgres

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]