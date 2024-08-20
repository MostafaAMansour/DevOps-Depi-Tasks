# Task 4: Nginx Configuration and Manual Docker Build

## Overview

In this task, you will configure Nginx to serve a PHP application along with static files and build a Docker image for a Java application. You will also run a Docker container exposing port 3000 and deploy the application within the container.

## Prerequisites

- **Operating System:** Linux or any Unix-based system.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.
- **Privileges:** You may need `sudo` privileges to execute certain commands.
- **Docker:** Ensure Docker is installed on your system.

## Instructions

### Nginx Configuration

1. **Create PHP Script and Static Files**

   - **PHP Script:** Create a PHP script to run `phpinfo()` and place it in `/var/www/html`.
   - **Static Files:** Create a folder named `static` in `/var/www/mobadra.com/html` and add two images named `pic.jpg` and `pic2.jpg`.

   - **Example PHP Script (`/var/www/html/index.php`):**
     ```php
     <?php
     phpinfo();
     ?>
     ```

2. **Configure Nginx**

   - **Objective:** Create a new configuration file for Nginx to serve the PHP application and static files.
   - **File Location:** `/etc/nginx/conf.d/mobadra.conf`
   - **Configuration File Content:**
     ```nginx
     server {
         listen 83;
         listen [::]:83;
         server_name mobadra.com;

         root /var/www/html;
         index index.php index.html index.htm;

         location / {
             try_files $uri $uri/ =404;
         }

         location ~ \.php$ {
             include snippets/fastcgi-php.conf;
             fastcgi_pass unix:/run/php/php-fpm.sock;
             #fastcgi_pass 127.0.0.1:9000;
             fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
             include fastcgi_params;
         }
         location ~ /\.ht {
             deny all;
         }

         location /static/ {
             root /var/www/mobadra.com/html;
             expires 30d;
             add_header Cache-Control "public";
             #gzip_static on;
             gzip on;
             gzip_types image/png image/jpeg image/gif;
             #gzip_proxied any;
             #gzip_min_length 256;
         }
     }
     ```

   - **Explanation:**
     - `listen 83;` and `listen [::]:83;` configure Nginx to listen on port 83 for both IPv4 and IPv6 connections.
     - `server_name mobadra.com;` specifies the domain name for this server block.
     - `root /var/www/html;` sets the root directory where Nginx will look for files.
     - `index index.php index.html index.htm;` specifies default index files.
     - `location ~ \.php$` handles PHP files using `fastcgi_pass` to communicate with the PHP processor handeled as socket, can be reconfigured by changing the comment of the two `fastcgi_pass` commands and add `listen = 127.0.0.1:9000` to /etc/php/7.3/fpm/pool.d/www.conf.
     - `location ~ /\.ht` denies access to `.ht` files.
     - `location /static/` serves static files from a different directory and enables caching and gzip compression.

3. **Test and Reload Nginx Configuration**

   - **Test Configuration:**
     ```bash
     sudo nginx -t
     ```
     - This command checks the syntax of your Nginx configuration files for errors.

   - **Reload Configuration:**
     ```bash
     sudo nginx -s reload
     ```
     - This command reloads the Nginx service to apply the new configuration without stopping the server.

### Docker Build and Run

1. **Clone the Spring Petclinic Repository**

   - **Objective:** Obtain the source code for the `spring-petclinic` application.
   - **Command:**
     ```bash
     git clone https://github.com/spring-projects/spring-petclinic.git
     ```

2. **Pull the Eclipse Temurin Docker Image**

   - **Objective:** Use the Eclipse Temurin image for running Java applications.
   - **Command:**
     ```bash
     sudo docker pull eclipse-temurin
     ```

3. **Build and Run Docker Container**

   - **Objective:** Build and run a Docker container for the Spring Petclinic application, exposing port 3000.
   - **Commands:**
     ```bash
     sudo docker run -d --name spring_petclinic_eclipse -p 3000:8080 --restart always eclipse-temurin
     ```

4. **Copy Application Files into the Container**

   - **Objective:** Copy the Spring Petclinic application files into the Docker container.
   - **Command:**
     ```bash
     sudo docker cp spring-petclinic spring_petclinic_eclipse:/app
     ```

5. **Access the Container and Run the Application**

   - **Objective:** Enter the container, build the application, and run it.
   - **Commands:**
     ```bash
     sudo docker exec -it spring_petclinic_eclipse /bin/bash
     cd /app
     ./mvnw package
     java -jar target/*.jar
     ```

## Summary

In this task, you configured Nginx to serve a PHP application along with static files and tested the configuration. You also built a Docker image for the Spring Petclinic Java application, ran a container, and deployed the application within the container. These steps provide experience with web server configuration and Docker container management.
