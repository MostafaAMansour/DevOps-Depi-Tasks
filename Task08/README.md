# Task 8: Docker Networking Manual and Using Docker Compose

## Overview

In this task, you will connect a `spring-petclinic` application with a MySQL database using Docker. You'll perform this setup both manually and using Docker Compose. This demonstrates how to manage multi-container applications and network them together effectively.

## Prerequisites

- **Docker:** Ensure Docker is installed and running on your system.
- **Docker Compose:** Ensure Docker Compose is installed.
- **Spring-Petclinic Application Image:** Ensure you have the Docker image for the `spring-petclinic` application.
- **MySQL Image:** Ensure you have access to the MySQL Docker image.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.

## Instructions

### Part 1: Manual Docker Networking

1. **Create a Docker Network**

   - **Objective:** Create a custom Docker network for inter-container communication.
   - **Command:**
     ```bash
     docker network create spring-net
     ```
   - **Explanation:** This command creates a Docker network named `spring-net` that will be used to connect the MySQL and `spring-petclinic` containers.

2. **Run MySQL Container**

   - **Objective:** Start a MySQL container with the necessary environment variables and volume.
   - **Command:**
     ```bash
     docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic --network spring-net --name spring_mysql_manual -v mysql_manual-volume:/var/lib/mysql -p 3009:3306 mysql
     ```
   - **Explanation:**
     - `-e` sets environment variables for MySQL.
     - `--network spring-net` connects the container to the `spring-net` network.
     - `-v mysql_manual-volume:/var/lib/mysql` mounts a Docker volume for persistent MySQL storage.
     - `-p 3009:3306` maps port 3306 in the container to port 3009 on the host.

3. **Run Spring-Petclinic Container**

   - **Objective:** Start a `spring-petclinic` container and connect it to the `spring-net` network.
   - **Command:**
     ```bash
     docker run -e MYSQL_URL=jdbc:mysql://spring_mysql_manual:3306/petclinic -e SPRING_PROFILES_ACTIVE=mysql --network spring-net --name spring_app_manual -p 3010:8080 spring_petclinic_eclipse
     ```
   - **Explanation:**
     - `-e MYSQL_URL=jdbc:mysql://spring_mysql_manual:3306/petclinic` sets the database URL for the application.
     - `-e SPRING_PROFILES_ACTIVE=mysql` activates the MySQL profile.
     - `--network spring-net` connects the container to the `spring-net` network.
     - `-p 3010:8080` maps port 8080 in the container to port 3010 on the host.

### Part 2: Docker Compose

1. **Create Docker Compose File**

   - **Objective:** Define and run the `spring-petclinic` application and MySQL database using Docker Compose.
   - **Docker Compose File (`docker-compose.yml`):**
     ```yaml
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
         ports:
           - "3008:3306"
         volumes:
           - db-volume:/var/lib/mysql

       app:
         build: .
         ports:
           - "3007:8080"
         depends_on:
           db:
             condition: service_healthy
         environment:
           - MYSQL_URL=jdbc:mysql://db:3306/petclinic
           - SPRING_PROFILES_ACTIVE=mysql

     volumes:
       db-volume:
     ```
   - **Explanation:**
     - `version: '3.8'` specifies the Docker Compose file format version.
     - `services` defines two services: `db` for MySQL and `app` for `spring-petclinic`.
     - `healthcheck` ensures that the MySQL container is healthy before starting the `app` service.
     - `depends_on` ensures that the `app` service waits for the `db` service to be healthy before starting.
     - `volumes` defines a volume `db-volume` for MySQL data persistence.

2. **Start Services with Docker Compose**

   - **Objective:** Build and run the containers defined in the Docker Compose file.
   - **Command:**
     ```bash
     docker-compose up
     ```
   - **Explanation:** This command builds and starts the services defined in the `docker-compose.yml` file.

## Summary

In this task, you configured Docker networking manually and using Docker Compose to connect a `spring-petclinic` application with a MySQL database. You created a Docker network for manual setup and used Docker Compose to manage multi-container applications efficiently. This demonstrates how Docker can be used for complex networking scenarios and service orchestration.
