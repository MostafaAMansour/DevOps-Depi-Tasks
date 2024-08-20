# Task 3: Nginx Introduction and Docker Installation

## Overview

In this task, you'll learn the basics of configuring Nginx and installing Docker. You'll set up a simple Nginx server to serve a static website and verify the installation and functionality of Docker by running a test image.

## Prerequisites

- **Operating System:** Linux or any Unix-based system.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.
- **Privileges:** You may need `sudo` privileges to execute certain commands.

## Instructions

### Nginx Configuration

1. **Create a Simple HTML File**

   - **Objective:** Create a basic `index.html` file to serve as the homepage of your website.
   - **Location:** Place the file in `/var/www/task_domain/html`.
   - **Example Content:**
     ```html
     <!DOCTYPE html>
     <html>
     <head>
         <title>Welcome to Task Domain</title>
     </head>
     <body>
         <h1>Hello, World!</h1>
         <p>This is a simple website served by Nginx.</p>
     </body>
     </html>
     ```
   - This file contains basic HTML that displays a heading and a paragraph when accessed through a web browser.

2. **Configure Nginx**

   - **Objective:** Create a new configuration file for Nginx to define how it should serve your website.
   - **File Location:** `/etc/nginx/conf.d/task_domain.conf`
   - **Configuration File Content:**
     ```nginx
     server {
         listen 82;
         listen [::]:82;

         index index.html index.htm index.nginx-debian.html;
         root /var/www/task_domain/html;
         server_name task_domain.event www.task_domain.event;

         location /static/ {
             root /var/www/mobadra.com/html;
         }

         location / {
             try_files $uri $uri/ =404;
         }
     }
     ```
   - **Explanation:**
     - `listen 82;` and `listen [::]:82;` configure Nginx to listen on port 82 for both IPv4 and IPv6 connections.
     - `index index.html index.htm index.nginx-debian.html;` specifies the default files to serve if a directory is requested.
     - `root /var/www/task_domain/html;` sets the root directory where Nginx will look for files.
     - `server_name task_domain.event www.task_domain.event;` specifies the domain names that this server block will respond to.
     - The `location /static/` block maps requests for `/static/` to a different directory.
     - The `location /` block serves files from the root directory and returns a 404 error if the file is not found.

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

### Docker Installation

1. **Install Docker**

   - **Objective:** Install Docker on your system.
   - **Instructions:** Follow the official Docker installation guide for your operating system. The general steps include:
     1. **Update Package Index:**
        ```bash
        sudo apt-get update
        ```
     2. **Install Required Packages:**
        ```bash
        sudo apt-get install apt-transport-https ca-certificates curl software-properties-common
        ```
     3. **Add Docker’s Official GPG Key:**
        ```bash
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
        ```
     4. **Add Docker Repository:**
        ```bash
        sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
        ```
     5. **Install Docker Engine:**
        ```bash
        sudo apt-get update
        sudo apt-get install docker-ce
        ```

2. **Run a Test Docker Image**

   - **Objective:** Verify that Docker is correctly installed and running.
   - **Command:**
     ```bash
     sudo docker run hello-world
     ```
   - **Explanation:**
     - This command downloads the `hello-world` Docker image (if not already downloaded) and runs it. The `hello-world` image is a simple container that outputs a message confirming that Docker is installed and functioning properly.

## Summary

In this task, you configured Nginx to serve a static website and validated the setup by creating a basic HTML file and configuring Nginx with a custom configuration. Additionally, you installed Docker and verified its functionality by running a test image. These steps provide foundational knowledge for web server management and containerization.
