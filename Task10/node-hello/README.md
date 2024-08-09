# Node Hello Application

## Overview

This README details the setup for the Node Hello application using Docker Compose with a load balancer. The Docker Compose configuration includes the application and a load balancer managed by Nginx.

## Prerequisites

- **Docker**: Docker must be installed and running.
- **Docker Compose**: Used to manage and deploy multi-container applications.
- **Node Hello Codebase**: Ensure you have the Node Hello application code available.

## Docker Compose Configuration

The `docker-compose.yml` file sets up the Node Hello application with a load balancer.

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build:
      context: .
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
      - /home/mostafa_abdallah/node-hello/conf.d:/etc/nginx/conf.d/
    ports:
      - 3013:80
    depends_on:
      - app
```

### Explanation:

`app`: Builds and deploys the Node Hello application with multiple replicas.
`load_balancer`: Configures Nginx as a load balancer to manage traffic to the application.

### Dockerfile

The Dockerfile for the Node Hello application sets up a simple Node.js environment.

```Dockerfile

# Dockerfile
FROM node:latest
WORKDIR /app
COPY . .
CMD ["npm", "start"]
```

### Explanation:

`Base Image`: Uses the latest Node.js image from Docker Hub.
`WORKDIR`: Sets the working directory inside the container to /app.
`COPY`: Copies the application code into the container.
`CMD`: Specifies the command to run the application (npm start).

