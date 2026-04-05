FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .

COPY eureka-server ./eureka-server

RUN chmod +x ./gradlew
RUN ./gradlew :eureka-server:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/eureka-server/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]