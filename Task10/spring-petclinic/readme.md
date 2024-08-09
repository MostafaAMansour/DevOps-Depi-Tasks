# Spring Petclinic Application

## Overview

This application demonstrates the deployment of the Spring Petclinic application using Docker with a load balancer. The setup includes Docker Compose configuration for orchestrating the application and a load balancer using Nginx.

## Prerequisites

- **Docker**: Ensure Docker is installed.
- **Docker Compose**: Required for orchestrating the services.
- **Spring Petclinic Image**: Docker image for the Spring Petclinic application.

## Instructions

### Docker Compose Configuration

The `docker-compose-loadbalancer.yml` file is used to define and run the multi-container setup for the Spring Petclinic application, including the application itself, a MySQL database, and a load balancer.

```yaml
# docker-compose-loadbalancer.yml
version: '3.8'

services:
  db:
    image: mysql:latest
    healthcheck:
      test: ['CMD-SHELL', 'mysqladmin ping -h 127.0.0.1 --password="root" --silent']
      interval: 3s
      retries: 5
      start_period: 30s
    environment:
      - MYSQL_USER=petclinic
      - MYSQL_PASSWORD=petclinic
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=petclinic
    volumes:
      - mysql-volume:/var/lib/mysql

  app:
    build:
      context: .
      target: dev
    depends_on:
      db:
        condition: service_healthy
    environment:
      - MYSQL_URL=jdbc:mysql://db:3306/petclinic
      - SPRING_PROFILES_ACTIVE=mysql
    deploy:
      replicas: 4
      update_config:
        parallelism: 2
        delay: 10s
      restart_policy:
        condition: on-failure
        delay: 5s

  load_balancer:
    image: nginx:latest
    volumes:
      - ./conf.d:/etc/nginx/conf.d/
    ports:
      - 3011:80
    depends_on:
      - app

volumes:
  mysql-volume:
```

### Explanation

`db`: Runs a MySQL container. It includes health checks to ensure the database is ready before the app starts. Environment variables configure the database.
`app`: Builds and runs the Spring Petclinic application. It waits for the database service to be healthy before starting. Configured with environment variables to connect to the MySQL database and has four replicas that the load_balancer balance between.
`load_balancer`: Runs an Nginx load balancer to distribute incoming traffic across the application instances.

### Dockerfile

```dockerfile
# Dockerfile
FROM spring_petclinic_eclipse:latest AS builder
WORKDIR /app
# RUN ./mvnw package # Uncomment if building from source

FROM eclipse-temurin:latest AS dev
WORKDIR /app
COPY --from=builder /app/target .
ENV STAGE="dev"
EXPOSE 8080
CMD ["/bin/sh", "-c", "java -jar *.jar"]

FROM eclipse-temurin:latest AS prod
WORKDIR /app
COPY --from=builder /app/target .
ENV STAGE="prod"
EXPOSE 8080
CMD ["/bin/sh", "-c", "java -jar *.jar"]
```

### Explanation

- `builder`: Base image for building the application. The WORKDIR is set and source code is copied. Uncomment the RUN command if building from source.
- `dev`: Development stage of the image. Copies the built application from the builder stage and sets the environment to "dev".
- `prod`: Production stage of the image. Similar to dev, but sets the environment to "prod".