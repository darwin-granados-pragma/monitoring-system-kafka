FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY applications/app-service/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
