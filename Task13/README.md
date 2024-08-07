# Task 13: Jenkins Pipeline Building Application

## Overview

In this task, I set up a Jenkins pipeline to automate the build, test, archive, and push processes for the Spring Petclinic application. The pipeline is configured to trigger automatically via a GitHub webhook, and it sends email notifications upon job success or failure.

## Jenkinsfile Configuration

The following `Jenkinsfile` was added to the Spring Petclinic application repository:

\`\`\`groovy
pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-MostafaAMansour'
        DOCKER_HUB_REPO = 'mostafaamansour/spring_petclinic'
        GITHUB_REPO = 'https://github.com/MostafaAMansour/spring-petclinic.git'
        GITHUB_CREDENTIALS = 'Github-MostafaAMansour'
        BRANCH = 'main'
        RECIPIENT = 'toota353535@gmail.com'
        ARTIFACT_DIR = 'artifacts'
    }

    stages {
        stage('Checkout') {
            steps {
                 script {
                     checkout([$class: 'GitSCM', branches: [[name: "${BRANCH}"]],
                         userRemoteConfigs: [[url: "${GITHUB_REPO}", credentialsId: "${GITHUB_CREDENTIALS}"]]
                     ])
                 }
            }
        }

        stage('Build') {
            steps {                
                script {
                    // Build the Docker images
                    sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml build'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    // Start services
                    sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml up -d'
                    // Run the test command
                    sh 'sleep 30' // wait for the service to be ready
                    // Execute the test command inside the app service
                    sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml exec app curl localhost:8080/owners/11 | grep Mostafa'
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                script {
                    // Create artifact directory if it doesn't exist
                    sh "mkdir -p ${ARTIFACT_DIR}"
                    // Copy artifacts from the container to the host
                    sh 'docker cp $(docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml ps -q app):/app/ ${ARTIFACT_DIR}'
                }
                archiveArtifacts artifacts: "${ARTIFACT_DIR}/app/*.jar", allowEmptyArchive: true
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    // Push the built images to Docker Hub
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_HUB_CREDENTIALS}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh """
                        echo "${PASSWORD}" | docker login -u "${USERNAME}" --password-stdin
                        docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml push
                        docker logout
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            mail subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                     body: "Good news! Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' succeeded.",
                     to: "${RECIPIENT}"
        }
        failure {
            mail subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                     body: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' failed. Please check the logs for details.",
                     to: "${RECIPIENT}"
        }
        cleanup {
            script {
                // List and remove containers, networks, images, and volumes specific to the project
                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml down'
            }
            // Clean up workspace and project-specific Docker containers, networks, images, and volumes
            cleanWs()
        }
    }
}
\`\`\`

## GitHub Webhook

Jenkins was configured to listen to a GitHub webhook to automatically trigger the pipeline upon code changes.

## Jenkins Credentials and SMTP Configuration

1. **Jenkins Credentials:**
   - **Docker Hub Credentials:** Stored under the ID `dockerhub-MostafaAMansour`. These credentials allow Jenkins to log in to Docker Hub and push the built Docker images.
   - **GitHub Credentials:** Stored under the ID `Github-MostafaAMansour`. These credentials enable Jenkins to clone the repository and interact with GitHub.

2. **SMTP Configuration:**
   - Configure Jenkins to use an SMTP server for sending emails. This is done in the Jenkins system configuration under "E-mail Notification" and "Extended E-mail Notification."
   - Ensure the `RECIPIENT` environment variable in the `Jenkinsfile` is set to the email address where notifications should be sent.

## Conclusion

This pipeline automates the CI/CD process for the Spring Petclinic application, ensuring that code changes are automatically built, tested, and deployed, with notifications sent to the team.
