# SKMZ Application

## Overview

This application showcases the SKMZ service using Docker Compose with a MongoDB database and a load balancer. The Docker setup includes building a Node.js and Go application with Docker Compose orchestration.

## Prerequisites

- **Docker**: Ensure Docker is installed.
- **Docker Compose**: Required for setting up and managing multi-container applications.
- **SKMZ Codebase**: Ensure the SKMZ application code is available.

## Instructions

### Docker Compose Configuration

The `docker-compose.yml` file is used to set up the SKMZ application with a MongoDB database and a load balancer.

```yaml
# docker-compose.yml
version: "3.7"

services:
  app:
    build: .
    depends_on:
      - db
    deploy:
      replicas: 4
      update_config:
        parallelism: 2
        delay: 10s
      restart_policy:
        condition: on-failure
        delay: 5s
    environment:
      profile: prod

  db:
    image: mongo:4.2.2
    environment:
      MONGO_INITDB_DATABASE: programmers
    volumes:
      - ./server/db/mongo.init:/docker-entrypoint-initdb.d/mongo-init.js

  load_balancer:
    image: nginx:latest
    volumes:
      - ./skmz/conf.d:/etc/nginx/conf.d/
    ports:
      - 3012:80
    depends_on:
      - app
```

### Explanation

- `app`: Builds and runs the SKMZ application, which depends on the MongoDB database. Configured to deploy with multiple replicas and restart policies.
- `db`: Runs a MongoDB container with initialization scripts provided through a volume.
- `load_balancer`: Uses Nginx to balance traffic across application instances.

### Dockerfile

The Dockerfile builds the SKMZ application, which includes Node.js and Go components.

```Dockerfile

# Dockerfile
FROM node:12.14 AS JS_BUILD
COPY webapp /webapp
WORKDIR webapp
RUN npm install && npm run build --prod

FROM golang:1.13.6-alpine AS GO_BUILD
COPY server /server
WORKDIR /server
RUN apk add build-base
RUN go build -o /go/bin/server

FROM alpine:3.11
COPY --from=JS_BUILD /webapp/build* ./webapp/
COPY --from=GO_BUILD /go/bin/server ./
CMD ./server

```

### Explanation

`JS_BUILD`: Builds the Node.js application by copying and building the web application.
`GO_BUILD`: Builds the Go application by copying the server code and building it.
`alpine`: Final runtime image that includes the built applications from the previous stages.
