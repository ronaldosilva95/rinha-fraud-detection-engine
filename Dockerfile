# Builder Stage - Compile the application
FROM container-registry.oracle.com/graalvm/native-image:25.0.2-ol9 AS builder

WORKDIR /build

# Copy the source code into the image for building
COPY . /build

# Build JAR first (more reliable for resources)
RUN ./mvnw clean package -DskipTests --no-transfer-progress

# Runtime Stage - Use slim JRE for faster startup and smaller image
FROM container-registry.oracle.com/os/oraclelinux:9-slim

# Install Java 25 JRE and curl for healthcheck
RUN microdnf install -y java-25-oraclejdk-headless curl && microdnf clean all

WORKDIR /app

# Copy the built JAR from builder
COPY --from=builder /build/target/fraud-detection-engine-0.0.1-SNAPSHOT.jar application.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ready || exit 1

ENTRYPOINT ["java", "-Xms128m", "-Xmx128m", "-jar", "application.jar"]
