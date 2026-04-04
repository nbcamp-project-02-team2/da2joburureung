FROM eclipse-temurin:17-jre

WORKDIR /app
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
