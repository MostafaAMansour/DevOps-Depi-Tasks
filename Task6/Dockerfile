FROM eclipse-temurin:latest

WORKDIR /app
COPY . .
EXPOSE 8080
RUN ./mvnw package
CMD ["/bin/sh", "-c", "java -jar /app/target/*.jar"]
