FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .

COPY common ./common
COPY product-service ./product-service

RUN chmod +x ./gradlew
RUN ./gradlew :product-service:clean :product-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

COPY --from=builder /app/product-service/build/libs/app.jar app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]