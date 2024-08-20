# Part 1: Nginx with Docker Volumes

## Overview

In this task, you'll learn how to use Docker volumes with Nginx to manage and persist configuration files and web content. You will set up two different Nginx containers: one for serving static files from a Docker volume and another for using a custom configuration file from a Docker volume.

## Instructions

1. ### Create Docker Volume and Run Nginx Container

   - **Objective:** Create a Docker volume for serving web content and run an Nginx container using this volume.
   - **Command:**
     ```bash
     docker run -d -p 3004:80 -v mobadra-volume:/usr/share/nginx/html --name nginx_mov_vol nginx
     ```
   - **Explanation:** This command runs an Nginx container named `nginx_mov_vol`, mapping port 3004 on the host to port 80 on the container. The `mobadra-volume` Docker volume is mounted to `/usr/share/nginx/html`, where Nginx serves web content.

2. ### Update Configuration and Run Nginx Container with Custom Config

   - **Objective:** Create and use a custom Nginx configuration file and run another Nginx container using this configuration.
   - **Commands:**
     ```bash
     docker run -d -p 3005:80 -v mobadra-volume:/usr/share/nginx/html -v conf-volume:/etc/nginx/conf.d --name nginx_mobconf_vol nginx
     ```
   - **Explanation:** This command runs an Nginx container named `nginx_mobconf_vol`, mapping port 3005 on the host to port 80 on the container. It uses two volumes: `mobadra-volume` for web content and `conf-volume` for Nginx configuration files.

3. ### Configure Nginx

   - **Objective:** Add a custom configuration file for Nginx.
   - **Configuration File Location:** `/var/lib/docker/volumes/conf-volume/_data/default.conf`
   - **Configuration File Content:**
     ```nginx
     server {
         listen       80;
         listen  [::]:80;
         server_name  localhost;

         #access_log  /var/log/nginx/host.access.log  main;

         location / {
             root   /usr/share/nginx/html;
             index  index.html index.htm;
         }

         #error_page  404              /404.html;

         # redirect server error pages to the static page /50x.html
         #
         error_page   500 502 503 504  /50x.html;
         location = /50x.html {
             root   /usr/share/nginx/html;
         }
     }
     ```

   - **Explanation:**
     - `listen 80;` configures Nginx to listen on port 80.
     - `root /usr/share/nginx/html;` sets the root directory for serving files.
     - `index index.html index.htm;` specifies default index files.
     - `error_page 500 502 503 504 /50x.html;` configures error pages.

4. ### Verify and Reload Nginx Configuration

   - **Objective:** Verify the Nginx configuration syntax and reload the configuration.
   - **Commands:**
     ```bash
     docker exec nginx_mobconf_vol nginx -t
     docker exec nginx_mobconf_vol nginx -s reload
     ```
   - **Explanation:**
     - `nginx -t` checks the syntax of the Nginx configuration files.
     - `nginx -s reload` reloads the Nginx service to apply changes.

## Summary

In this task, you configured Docker volumes to manage and persist Nginx configuration files and web content. You ran Nginx containers with volumes for static files and custom configurations, and verified the setup by reloading the Nginx configuration. This process demonstrates how Docker volumes can be used to handle persistent data and configuration in containerized applications.
