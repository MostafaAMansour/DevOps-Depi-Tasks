# Task 6: Dockerfile Building Basics

## Overview

In this task, you will create a Dockerfile to build a Docker image for the `spring-petclinic` application, and then run a container from this image. This exercise demonstrates the basics of writing a Dockerfile and building and running Docker containers.

## Prerequisites

- **Docker:** Ensure Docker is installed and running on your system.
- **Spring Petclinic Application:** The application code should be available in the directory where you create the Dockerfile.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.

## Instructions

### 1. Create the Dockerfile

   - **Objective:** Write a Dockerfile to build an image for the `spring-petclinic` application.
   - **Dockerfile Content:**
     ```dockerfile
     FROM eclipse-temurin:latest
     WORKDIR /app
     COPY . .
     EXPOSE 8080
     RUN ./mvnw package
     CMD ["/bin/sh", "-c", "java -jar /app/target/*.jar"]
     ```
   - **Explanation:**
     - `FROM eclipse-temurin:latest`: Uses the latest Eclipse Temurin image as the base image.
     - `WORKDIR /app`: Sets the working directory inside the container to `/app`.
     - `COPY . .`: Copies the current directory’s contents into the `/app` directory inside the container.
     - `EXPOSE 8080`: Exposes port 8080 on the container.
     - `RUN ./mvnw package`: Executes the Maven wrapper to build the application.
     - `CMD ["/bin/sh", "-c", "java -jar /app/target/*.jar"]`: Defines the command to run the application using `java -jar`.

### 2. Build the Docker Image

   - **Objective:** Build a Docker image from the Dockerfile.
   - **Command:**
     ```bash
     sudo docker build -t spring_petclinic_eclipse .
     ```
   - **Explanation:** This command builds a Docker image with the tag `spring_petclinic_eclipse` from the Dockerfile in the current directory (`.`).

### 3. Run the Docker Container

   - **Objective:** Run a Docker container from the built image and expose port 3000.
   - **Command:**
     ```bash
     sudo docker run -d --name spring_petclinic_ec0 -p 3000:8080 spring_petclinic_eclipse
     ```
   - **Explanation:** This command runs a container named `spring_petclinic_ec0` in detached mode (`-d`), maps port 3000 on the host to port 8080 in the container, and uses the `spring_petclinic_eclipse` image.

## Summary

In this task, you created a Dockerfile to define how the `spring-petclinic` application should be built and run inside a Docker container. You then built the Docker image from this Dockerfile and ran a container, exposing port 3000 to access the application. This process demonstrates the basics of Dockerfile creation, image building, and container management.
