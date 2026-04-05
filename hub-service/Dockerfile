FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :hub-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/hub-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]