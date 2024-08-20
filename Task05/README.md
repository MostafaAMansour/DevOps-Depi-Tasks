# Task 5: Docker Image Backing Up

## Overview

In this task, you'll learn how to back up a Docker image by committing a container, pushing it to Docker Hub, and saving it to a tar file. This process is essential for preserving and transferring Docker images.

## Prerequisites

- **Docker:** Ensure Docker is installed and running on your system.
- **Docker Hub Account:** You need an account on Docker Hub for pushing images.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.

## Instructions

### 1. Commit the Docker Container

   - **Objective:** Commit the Docker container to create a new image with a specified tag.
   - **Command:**
     ```bash
     sudo docker commit spring_petclinic_eclipse mostafaamansour/spring_petclinic:v1
     ```
   - **Explanation:** This command creates a new image named `mostafaamansour/spring_petclinic` with the tag `v1` from the `spring_petclinic_eclipse` container.

### 2. Log In to Docker Hub

   - **Objective:** Authenticate with Docker Hub to allow pushing the image.
   - **Command:**
     ```bash
     sudo docker login
     ```
   - **Explanation:** This command prompts you for your Docker Hub username and password to log in.

### 3. Push the Docker Image to Docker Hub

   - **Objective:** Upload the Docker image to Docker Hub for storage and sharing.
   - **Command:**
     ```bash
     sudo docker push mostafaamansour/spring_petclinic:v1
     ```
   - **Explanation:** This command uploads the `mostafaamansour/spring_petclinic:v1` image to your Docker Hub repository.

### 4. Save the Docker Image to a Tar File

   - **Objective:** Export the Docker image to a tar file for backup or transfer.
   - **Command:**
     ```bash
     sudo docker save -o spring-petclinic.tar mostafaamansour/spring_petclinic:v1
     ```
   - **Explanation:** This command saves the `mostafaamansour/spring_petclinic:v1` image to a file named `spring-petclinic.tar`.

## Summary

In this task, you committed a Docker container to create a new image, logged in to Docker Hub, pushed the image to Docker Hub for remote storage, and saved the image to a tar file for local backup or transfer. These steps are crucial for managing Docker images and ensuring they can be preserved and shared as needed.
