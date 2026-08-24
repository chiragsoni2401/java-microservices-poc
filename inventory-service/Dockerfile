FROM openjdk:21-slim
WORKDIR /app
COPY target/inventory-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
