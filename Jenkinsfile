pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'docker-registry'
        IMAGE_NAME = 'study-service'
        IMAGE_TAG = 'latest'
        DEPLOY_SERVER_IP = 'raspberry-pi-ip'
        DEPLOY_SERVER_USER = 'pi'

        DOCKER_CREDENTIALS_ID = 'dockerhub'
        SSH_CREDENTIALS_ID = 'raspberry-pi-ssh'
    }

    stages {
        stage('Submodule Pull') {
            steps {
                sh 'git submodule init'
                sh 'git submodule update --remote --recursive'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    sh """
                    docker build --platform linux/amd64 -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} .
                    """
                }

            }
        }
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        sh """
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        """
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    sshagent([SSH_CREDENTIALS_ID]) {
                        sh """
                        ssh -p 22123 ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} 'docker stop ${IMAGE_NAME} || true'
                        ssh -p 22123 ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} 'docker rm ${IMAGE_NAME} || true'
                        ssh -p 22123 ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} 'docker rmi ${DOCKER_REGISTRY}/${IMAGE_NAME} || true'
                        ssh -p 22123 ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} 'docker pull ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
                        ssh -p 22123 ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} 'docker run --platform linux/amd64 -d -p 56789:56789 --name ${IMAGE_NAME} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
                        """
                    }
                }
            }
        }
    }
}

