FROM spring_petclinic_eclipse:latest AS builder
WORKDIR /app

FROM eclipse-temurin:latest AS dev
WORKDIR /app
COPY --from=builder /app/target .
ENV PORT=8080
EXPOSE 8080
CMD ["/bin/sh", "-c", "java -jar *.jar"]

FROM eclipse-temurin:latest AS prod
WORKDIR /app
COPY --from=builder /app/target .
ENV PORT=8081
EXPOSE 8080
CMD ["/bin/sh", "-c", "java -jar *.jar"]
