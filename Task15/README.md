# Task 15: Jenkins Pipeline DSL Project

## Overview

This project demonstrates a Jenkins pipeline configuration using DSL to create and manage multiple jobs. The pipeline consists of three jobs that run sequentially: `BuildAndTest`, `ArchiveAndPush`, and `PostBuildActions`.

## Part 1: Job - Spring-petclinic-DSL-BuildAndTest

This job is responsible for checking out the code, building the Docker images, running tests, and archiving the build artifacts.

### Jenkins DSL Code

```groovy
pipelineJob('Spring-petclinic-DSL-BuildAndTest') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    environment {
                        GITHUB_REPO = 'https://github.com/MostafaAMansour/spring-petclinic.git'
                        GITHUB_CREDENTIALS = 'Github-MostafaAMansour'
                        BRANCH = 'main'
                    }

                    stages {
                        stage('Checkout') {
                            steps {
                                git branch: "\${BRANCH}", credentialsId: "\${GITHUB_CREDENTIALS}", url: "\${GITHUB_REPO}"
                            }
                        }

                        stage('Build') {
                            steps {                
                                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml build'
                            }
                        }

                        stage('Test') {
                            steps {
                                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml up -d'
                                sh 'sleep 30' // wait for the service to be ready
                                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml exec app curl localhost:8080/owners/11 | grep Mostafa'
                            }
                        }

                        stage('Archive Files') {
                            steps {
                                archiveArtifacts artifacts: '**/*'
                            }
                        }
                    }

                    post {
                        always {
                            script {
                                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml down'
                            }
                            cleanWs()
                        }
                        success {
                            build job: 'Spring-petclinic-DSL-ArchiveAndPush', wait: false
                        }
                        failure {
                            build job: 'Spring-petclinic-DSL-PostBuildActions', parameters: [string(name: 'JOB_STATUS', value: 'FAILURE')], wait: false
                        }
                    }
                }
            """)
        }
    }
}
```
### Explanation

- **Checkout:** Retrieves the code from the GitHub repository using specified credentials and branch.
- **Build:** Builds Docker images using Docker Compose.
- **Test:** Runs the Docker containers, waits for the application to be ready, and performs a basic health check.
- **Archive Files:** Archives build artifacts for later use.
- **Post Actions:** Cleans up Docker containers and workspaces, and triggers subsequent jobs based on the build result.

## Part 2: Job - Spring-petclinic-DSL-ArchiveAndPush

This job handles unarchiving build artifacts, archiving application artifacts, and pushing Docker images to Docker Hub.

### Jenkins DSL Code

```groovy
pipelineJob('Spring-petclinic-DSL-ArchiveAndPush') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    environment {
                        DOCKER_HUB_CREDENTIALS = 'dockerhub-MostafaAMansour'
                        DOCKER_HUB_REPO = 'mostafaamansour/spring_petclinic'
                        ARTIFACT_DIR = 'artifacts'
                    }

                    stages {
                        stage('Unarchive Files') {
                            steps {
                                copyArtifacts projectName: 'Spring-petclinic-DSL-BuildAndTest', filter: '**/*', selector: lastSuccessful()
                            }
                        }

                        stage('Archive Artifacts') {
                            steps {
                                sh "mkdir -p \${ARTIFACT_DIR}"
                                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml up -d'
                                sh 'docker cp \$(docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml ps -q app):/app/ \${ARTIFACT_DIR}'
                                archiveArtifacts artifacts: "\${ARTIFACT_DIR}/app/*.jar", allowEmptyArchive: true
                            }
                        }

                        stage('Push Docker Images') {
                            steps {
                                withCredentials([usernamePassword(credentialsId: "\${DOCKER_HUB_CREDENTIALS}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                    sh '''
                                    echo "\${PASSWORD}" | docker login -u "\${USERNAME}" --password-stdin
                                    docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml push
                                    docker logout
                                    '''
                                }
                            }
                        }
                    }

                    post {
                        always {
                            script {
                                sh 'docker compose -f spring-petclinic/docker-compose.yml -f spring-petclinic/docker-compose-dev.yml down'
                            }
                            cleanWs()
                            build job: 'Spring-petclinic-DSL-PostBuildActions', parameters: [string(name: 'JOB_STATUS', value: 'SUCCESS')], wait: false
                        }
                    }
                }
            """)
        }
    }
}
```

### Explanation

- **Unarchive Files:** Copies artifacts from the BuildAndTest job.
- **Archive Artifacts:** Archives application artifacts from the Docker container.
- **Push Docker Images:** Pushes the Docker images to Docker Hub.
- **Post Actions:** Cleans up Docker containers and workspaces, and triggers the final job.

## Part 3: Job - Spring-petclinic-DSL-PostBuildActions

This job sends email notifications based on the status of the previous jobs.

### Jenkins DSL Code

```groovy
pipelineJob('Spring-petclinic-DSL-PostBuildActions') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    environment {
                        RECIPIENT = 'toota353535@gmail.com'
                        JOB_STATUS = 'SUCCESS'
                    }

                    stages {
                        stage('Post-Build Notification') {
                            steps {
                                script {
                                    if (env.JOB_STATUS == 'SUCCESS') {
                                        mail subject: "SUCCESS: Job '\${env.JOB_NAME} [\${env.BUILD_NUMBER}]'",
                                             body: "Good news! Job '\${env.JOB_NAME} [\${env.BUILD_NUMBER}]' succeeded.",
                                             to: "\${RECIPIENT}"
                                    } else {
                                        mail subject: "FAILURE: Job '\${env.JOB_NAME} [\${env.BUILD_NUMBER}]'",
                                             body: "Job '\${env.JOB_NAME} [\${env.BUILD_NUMBER}]' failed. Please check the logs for details.",
                                             to: "\${RECIPIENT}"
                                    }
                                }
                            }
                        }
                    }

                    post {
                        always {
                            cleanWs()
                        }
                    }
                }
            """)
        }
    }
}
```
### Explanation

- **Post-Build Notification:** Sends an email based on the result of the pipeline, indicating success or failure.
- **Post Actions:** Cleans up workspaces after sending notifications.

## Conclusion

This Jenkins pipeline setup uses DSL to create and manage a series of jobs that build, test, archive, and push Docker images, and notify stakeholders about the build status. This approach streamlines the CI/CD process and ensures that all necessary steps are handled automatically.