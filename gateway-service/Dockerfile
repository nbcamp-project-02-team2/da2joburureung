FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .

COPY common ./common
COPY gateway-service ./gateway-service

RUN chmod +x ./gradlew
RUN ./gradlew :gateway-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/gateway-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]