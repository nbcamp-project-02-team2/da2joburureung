FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .

COPY common ./common
COPY company-service ./company-service

RUN chmod +x ./gradlew
RUN ./gradlew :company-service:clean :company-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/company-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]