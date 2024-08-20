# Part 2: Node-Hello Application with Docker

## Overview

In this task you'll build and run a Docker container for a Node.js application

1. ### Create the Dockerfile for Node.js Application

   - **Objective:** Write a Dockerfile to build an image for the `node-hello` application.
   - **Dockerfile Content:**
     ```dockerfile
     FROM node:latest
     WORKDIR /app
     COPY . .
     CMD ["npm", "start"]
     ```
   - **Explanation:**
     - `FROM node:latest`: Uses the latest Node.js image as the base image.
     - `WORKDIR /app`: Sets the working directory inside the container to `/app`.
     - `COPY . .`: Copies the current directory’s contents into the `/app` directory inside the container.
     - `CMD ["npm", "start"]`: Defines the command to start the Node.js application.

2. ### Build the Docker Image

   - **Objective:** Build a Docker image from the Dockerfile.
   - **Command:**
     ```bash
     sudo docker build -t node-hello-app .
     ```
   - **Explanation:** This command builds a Docker image with the tag `node-hello-app` from the Dockerfile in the current directory (`.`).

3. ### Run the Docker Container

   - **Objective:** Run a Docker container from the built Node.js image.
   - **Command:**
     ```bash
     sudo docker run -d --name node-hello-container -p 3006:3000 node-hello-app
     ```
   - **Explanation:** This command runs a container named `node-hello-container` in detached mode (`-d`), mapping port 3006 on the host to port 3000 in the container, and uses the `node-hello-app` image.

## Summary

In this task, you learned to manage Docker volumes for Nginx configuration and web content, and you built and ran a Docker container for a Node.js application. You configured Nginx to use Docker volumes for static content and custom configurations and set up a Node.js application in Docker to run as a service. This task highlights how Docker volumes can be used for persistent data and configuration, and how to build and run Node.js applications in Docker containers.