# Task 9: Multilevel Dockerfile and Targeted Build with Multifile Docker Compose

## Overview

In this task, you'll work with multistage Dockerfiles to create optimized Docker images for different environments and use multifile Docker Compose configurations to manage services for these environments. The goal is to understand how to build Docker images with different stages and how to manage different configurations for development and production environments using Docker Compose.

## Prerequisites

- **Docker:** Ensure Docker is installed and running on your system.
- **Docker Compose:** Ensure Docker Compose is installed for managing multi-container applications.
- **Git:** Ensure Git is installed to clone repositories.

## Instructions

### 1. Create a Multilevel Dockerfile

Create a Dockerfile that uses multistage builds to separate the build and runtime environments. The Dockerfile should have three stages:

- **Builder Stage:** Use `spring_petclinic_eclipse:latest` to build the application.
- **Development Stage:** Use `eclipse-temurin:latest` with `STAGE=DEV`.
- **Production Stage:** Use `eclipse-temurin:latest` with `STAGE=PROD`.

Create the `Dockerfile` with the following content:

```dockerfile
FROM spring_petclinic_eclipse:latest AS builder
WORKDIR /app

FROM eclipse-temurin:latest AS dev
WORKDIR /app
COPY --from=builder /app/target .
ENV STAGE=DEV
EXPOSE 8080
CMD ["/bin/sh", "-c", "java -jar *.jar"]

FROM eclipse-temurin:latest AS prod
WORKDIR /app
COPY --from=builder /app/target .
ENV STAGE=PROD
EXPOSE 8080
CMD ["/bin/sh", "-c", "java -jar *.jar"]
```

### 2. Create Docker Compose Files

#### 2.1. `docker-compose.yml`

This file contains the common configuration shared by development and production environments. Create the `docker-compose.yml` with the following content:

```yaml
services:
  app:
    ports:
      - "3008:8080"
    depends_on:
      db:
        condition: service_healthy
```

#### 2.2. `docker-compose-dev.yml`

This file defines the development environment using MySQL. Create the `docker-compose-dev.yml` with the following content:

```yaml
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
    ports:
      - "3007:3306"
  app:
    build:
      context: .
      target: dev
    environment:
      - MYSQL_URL=jdbc:mysql://db:3306/petclinic
      - SPRING_PROFILES_ACTIVE=mysql
volumes:
  mysql-volume:
```

#### 2.3. `docker-compose-prod.yml`

This file defines the production environment using PostgreSQL. Create the `docker-compose-prod.yml` with the following content:

```yaml
services:
  db:
    image: postgres:latest
    environment:
      - POSTGRES_USER=petclinic
      - POSTGRES_PASSWORD=petclinic
      - POSTGRES_DB=petclinic
    healthcheck:
      test: ["CMD-SHELL", "sh -c 'pg_isready -U petclinic -d petclinic'"]
      interval: 10s
      timeout: 3s
      retries: 3
    volumes:
      - postgres-volume:/var/lib/postgresql/data
    ports:
      - "3007:5432"
  app:
    build:
      context: .
      target: prod
    environment:
      - POSTGRES_USER=petclinic
      - POSTGRES_PASSWORD=petclinic
      - POSTGRES_URL=jdbc:postgresql://db:5432/petclinic
      - SPRING_PROFILES_ACTIVE=postgres
volumes:
  postgres-volume:
```

## Summary

In this task, you've created a multistage Dockerfile to build optimized images for different environments and used multifile Docker Compose configurations to manage service definitions for development and production environments. This approach helps in maintaining a clean build process and managing different configurations effectively.
