# Task 14: Jenkins Docker in Docker, GitHub Webhook, and Agent on Windows Machine

## Overview

This task involves setting up Jenkins with Docker in Docker (DinD) capability, configuring a GitHub webhook to trigger Jenkins jobs, and creating a Jenkins agent on a Windows machine. Below are the detailed steps and explanations for each part.

## Part 1: Setting Up Jenkins Using Docker Image

To set up Jenkins with Docker in Docker (DinD) support, follow these steps:

### Dockerfile for Jenkins Setup

Create a Dockerfile with the following content:

\`\`\`dockerfile
FROM jenkins/jenkins:latest
USER root
RUN apt update && curl -fsSL https://get.docker.com | sh
RUN usermod -aG docker jenkins
USER jenkins
\`\`\`

### Explanation

- **Base Image:** `jenkins/jenkins:latest` is used as the base image for Jenkins.
- **Switch to Root User:** The `USER root` command allows root-level privileges to install Docker.
- **Docker Installation:** `apt update && curl -fsSL https://get.docker.com | sh` updates the package list and installs Docker using the official Docker installation script.
- **Add Jenkins User to Docker Group:** `usermod -aG docker jenkins` adds the Jenkins user to the Docker group, enabling it to run Docker commands.
- **Switch Back to Jenkins User:** `USER jenkins` switches back to the Jenkins user for running Jenkins processes.

### Running the Jenkins Container

Run the Jenkins container using the following command:

\`\`\`bash
docker run -dit --restart=always -p 8081:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock --name jenkins --group-add 999 jenkinsagent
\`\`\`

### Explanation

- **Detached Mode:** `-dit` runs the container in detached mode.
- **Restart Policy:** `--restart=always` ensures the container restarts automatically if it stops.
- **Port Mapping:** `-p 8081:8080` maps the Jenkins web interface to port 8081 on the host, and `-p 50000:50000` maps the Jenkins agent port.
- **Volume Mounts:**
  - `-v jenkins_home:/var/jenkins_home` mounts the Jenkins home directory to preserve data.
  - `-v /var/run/docker.sock:/var/run/docker.sock` mounts the Docker socket to enable Docker commands from within Jenkins.
- **Group Add:** `--group-add 999` ensures Jenkins has access to the Docker group.
- **Container Name:** `--name jenkins` names the container "jenkins".

## Part 2: Triggering by Webhook Using GitHub and ngrok

To automatically trigger Jenkins jobs using a GitHub webhook, follow these steps:

### GitHub Webhook Setup

1. **GitHub Repository Configuration:**
   - Go to your GitHub repository settings.
   - Under the "Webhooks" section, add a new webhook.
   - Set the "Payload URL" to the public URL provided by ngrok (e.g., `http://<ngrok-public-url>/github-webhook/`).
   - Choose the `application/json` content type.
   - Select the events you want Jenkins to listen to (e.g., push events).
   - Save the webhook.

### ngrok Setup

Use ngrok to expose Jenkins to the public internet temporarily:

\`\`\`bash
ngrok http 8081
\`\`\`

### Explanation

- **ngrok:** ngrok is a tool that creates a secure tunnel to your local machine, giving it a temporary public IP. It’s essential for allowing GitHub to communicate with your local Jenkins server during development.

### Jenkins Job Configuration

1. **Create a New Jenkins Job:**
   - Go to Jenkins, click "New Item," and create a new job (e.g., Freestyle or Pipeline).
   - In the job configuration, under "Build Triggers," check the "GitHub hook trigger for GITScm polling" option.

2. **Set Up GitHub Project:**
   - In the job configuration, under "Source Code Management," choose Git and add your GitHub repository URL.
   - Ensure your repository credentials are configured.

3. **Testing the Webhook:**
   - Push changes to the configured GitHub repository, and Jenkins should trigger the job automatically.

## Part 3: Creating an Agent on a Windows Machine

To create and configure a Jenkins agent on a Windows machine, follow these steps:

### Jenkins Agent Setup

1. **Install Java on Windows:**
   - Download and install the latest version of Java on the Windows machine. Ensure the JAVA_HOME environment variable is set.

2. **Download Jenkins Agent JAR:**
   - Go to the Jenkins web interface.
   - Navigate to "Manage Jenkins" > "Manage Nodes and Clouds."
   - Click "New Node" and configure it as a permanent agent.
   - Once the agent is created, go to the agent’s configuration page and download the `agent.jar` file.

3. **Run the Jenkins Agent:**
   - Open a command prompt on the Windows machine.
   - Navigate to the directory where `agent.jar` is downloaded.
   - Run the following command:

\`\`\`bash
java -jar agent.jar -jnlpUrl <JENKINS_URL>/computer/<AGENT_NAME>/slave-agent.jnlp -secret <SECRET> -workDir "C:\jenkins"
\`\`\`

### Explanation

- **jnlpUrl:** The URL provided in the Jenkins agent configuration.
- **secret:** The secret key provided by Jenkins for authentication.
- **workDir:** The working directory for Jenkins jobs on the agent machine.

### Verify the Agent

- After running the command, the agent should connect to Jenkins, and the status should change to "Online" in the Jenkins web interface.

## Conclusion

This setup allows Jenkins to run within a Docker container, utilize Docker for building and testing applications, trigger jobs via GitHub webhooks, and distribute workloads to an agent running on a Windows machine. This configuration enhances Jenkins' CI/CD capabilities across different environments.
