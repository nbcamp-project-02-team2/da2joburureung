FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 루트 빌드 설정 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# common 복사
COPY common ./common

# delivery_route_record-service 소스 복사
COPY delivery_route_record-service ./delivery_route_record-service

RUN chmod +x gradlew
RUN ./gradlew :delivery_route_record-service:clean :delivery_route_record-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/delivery_route_record-service/build/libs/*.jar app.jar

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "app.jar"]
