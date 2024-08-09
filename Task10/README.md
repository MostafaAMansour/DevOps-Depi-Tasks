# Task 10: Three Complete Applications Using Load Balancer and Docker Compose

## Overview

This repository contains Dockerized versions of three applications:
- **Spring Petclinic**
- **SKMZ**
- **Node-Hello**

Each application is containerized using Docker, and Docker Compose is used to manage the containers. Additionally, Nginx is used as a load balancer for the applications.

## Prerequisites

- **Docker**: Ensure Docker is installed on your system.
- **Docker Compose**: Make sure Docker Compose is available to orchestrate multi-container applications.

## Table of Contents

1. [Spring Petclinic](./spring-petclinic/README.md)
2. [SKMZ](./skmz/README.md)
3. [Node Hello](./node-hello/README.md)

## Running the Applications

To run all three applications with a load balancer, follow these steps:

1. **Clone the repository:**
    ```bash
    git clone https://github.com/MostafaAMansour/DevOps-Depi-Tasks/Task10/app-name.git
    cd app-name
    ```

2. **Start the applications with Docker Compose:**
    ```bash
    docker-compose -f docker-compose-loadbalancer.yml up
    docker-compose up
    ```

This command will start all services including the applications and the Nginx load balancer.
note that first one for spring-petclinic and second one for the othe two.


## Nginx Configuration

For all applications, the Nginx configuration is:

```nginx
server {
    listen 80;
    location / {
        proxy_pass http://app:port-num;
    }
}
```

Note that the port-num is specific to each application:

Spring Petclinic: Port `8080`
SKMZ: Port `8080`
Node-Hello: Port `3000`