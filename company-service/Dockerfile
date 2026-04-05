FROM eclipse-temurin:17-jre

WORKDIR /app

COPY company-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]