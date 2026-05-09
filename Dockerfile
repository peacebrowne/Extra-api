# Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

EXPOSE 8080

COPY target/extra-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]