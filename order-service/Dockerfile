FROM eclipse-temurin:17-jre

WORKDIR /app
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

COPY build/libs/app.jar app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
