FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .

COPY common ./common
COPY order-service ./order-service

RUN chmod +x ./gradlew
RUN ./gradlew :order-service:clean :order-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

COPY --from=builder /app/order-service/build/libs/app.jar app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]