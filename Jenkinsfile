pipeline {
    agent any

    // 전체 파이프라인에 영향을 미치는 옵션
    options {
        timeout(time: 1, unit: 'HOURS') // 각 빌드 실행 시간을 1시간으로 제한
        skipStagesAfterUnstable()      // 이전 단계가 불안정할 경우 이후 단계를 건너뜀
    }

    environment {
        IMAGE_NAME = 'study-service'
        IMAGE_TAG = 'latest'
        DOCKER_REGISTRY = 'docker-registry'
        DEPLOY_SERVER_IP = 'raspberry-pi-ip'
        DEPLOY_SERVER_USER = 'pi'

        DOCKER_CREDENTIALS_ID = 'dockerhub'
        SSH_CREDENTIALS_ID = 'raspberry-pi-ssh'
        GIT_CREDENTIALS_ID = 'github_access_token' 
        DISCORD_CREDENTIALS_ID = 'discord_webhook'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']], // 필요한 브랜치 이름으로 변경
                    extensions: [[$class: 'SubmoduleOption', recursiveSubmodules: true, trackingSubmodules: true]],
                    userRemoteConfigs: [[
                        url: 'https://github.com/dev-trailblazers/secrets.git',
                        credentialsId: "${GIT_CREDENTIALS_ID}"
                    ]]
                ])
            }
        }
        stage('Build') {
            steps {
                script {
                    sh './gradlew clean build -x test'  //CI에서 테스트를 진행했기 때문에 CD에서는 테스트를 제외
                }
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    sh "docker build --platform linux/amd64 -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }
        }
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
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
                        ssh -p 22123 ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} 'docker run -d -p 56789:56789 --name ${IMAGE_NAME} ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            // 작업공간 정리
            cleanWs() 
            //Discord로 빌드 결과 전송
            withCredentials([string(credentialsId: DISCORD_CREDENTIALS_ID, variable: 'DISCORD_WEBHOOK_URL')]) {
                discordSend(
                    description: "Jenkins Build Notification",
                    footer: "Jenkins Pipeline",
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    title: "${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    webhookURL: "${DISCORD_WEBHOOK_URL}",
                    color: currentBuild.currentResult == 'SUCCESS' ? 'GOOD' : 'DANGER'
                )
            }

        }
    }
}
