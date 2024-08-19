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
                            build job: 'ArchiveAndPush', wait: false
                        }
                        failure {
                            build job: 'PostBuildActions', parameters: [string(name: 'JOB_STATUS', value: 'FAILURE')], wait: false
                        }
                    }
                }
            """)
        }
    }
}

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
                                copyArtifacts projectName: 'BuildAndTest', filter: '**/*', selector: lastSuccessful()
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
                            build job: 'PostBuildActions', parameters: [string(name: 'JOB_STATUS', value: 'SUCCESS')], wait: false
                        }
                    }
                }
            """)
        }
    }
}

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
