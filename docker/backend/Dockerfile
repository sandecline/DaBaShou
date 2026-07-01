# =============================================================================
# DaBaShou Backend - Multi-stage Dockerfile
# Stage 1: Build with Maven
# Stage 2: Runtime with JRE 21
# =============================================================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom files first for dependency caching
COPY pom.xml .
COPY dabashou-common/pom.xml dabashou-common/
COPY dabashou-system/pom.xml dabashou-system/
COPY dabashou-user/pom.xml dabashou-user/
COPY dabashou-skill/pom.xml dabashou-skill/
COPY dabashou-shelf/pom.xml dabashou-shelf/
COPY dabashou-demand/pom.xml dabashou-demand/
COPY dabashou-order/pom.xml dabashou-order/
COPY dabashou-point/pom.xml dabashou-point/
COPY dabashou-message/pom.xml dabashou-message/
COPY dabashou-credit/pom.xml dabashou-credit/
COPY dabashou-stat/pom.xml dabashou-stat/
COPY dabashou-admin/pom.xml dabashou-admin/
COPY dabashou-api/pom.xml dabashou-api/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY dabashou-common/src dabashou-common/src
COPY dabashou-system/src dabashou-system/src
COPY dabashou-user/src dabashou-user/src
COPY dabashou-skill/src dabashou-skill/src
COPY dabashou-shelf/src dabashou-shelf/src
COPY dabashou-demand/src dabashou-demand/src
COPY dabashou-order/src dabashou-order/src
COPY dabashou-point/src dabashou-point/src
COPY dabashou-message/src dabashou-message/src
COPY dabashou-credit/src dabashou-credit/src
COPY dabashou-stat/src dabashou-stat/src
COPY dabashou-admin/src dabashou-admin/src
COPY dabashou-api/src dabashou-api/src

# Build the application
RUN mvn clean package -DskipTests -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# Copy JAR from builder
COPY --from=builder /build/dabashou-api/target/dabashou-api-*.jar app.jar

# Create directories for logs and temp files
RUN mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
