#Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

#Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/build/libs/*.jar app.jar
USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# SubTask 1: profile docker-no-db
# SubTask 2: profile being overwritten docker-compose env SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]