pipeline {
    agent any

    options {
        timeout(time: 1, unit: 'HOURS')
        skipStagesAfterUnstable()
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
        DISCORD_CREDENTIALS_ID = 'discord-webhook'
    }

    stages {
        stage('Git Clone & submodule init') {
            steps {
                checkout scmGit(
                    branches: [[name: 'main']],
                    extensions: [
                        submodule(
                            parentCredentials: true,
                            recursiveSubmodules: true,
                            trackingSubmodules: true
                        )
                    ],
                    userRemoteConfigs: [[
                        url: 'https://github.com/dev-trailblazers/study-server',
                        credentialsId: "${GIT_CREDENTIALS_ID}"
                    ]]
                )
                // 수동으로 서브모듈 업데이트
                sh 'git submodule update --init --recursive --remote'
            }
        }
        stage('Build') {
            steps {
                script {
                    // CI에서 테스트를 진행했기 때문에 테스트나 기타 작업을 제외하고 Jar만 생성
                    sh './gradlew clean bootJar'
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
            script {
                // Discord로 빌드 결과 전송
                withCredentials([string(credentialsId: DISCORD_CREDENTIALS_ID, variable: 'DISCORD_WEBHOOK_URL')]) {
                    discordSend(
                        description: """
                            **Study Service Monolithic CICD #${env.BUILD_NUMBER}**
                            **Status**: ${currentBuild.currentResult}
                            **프로젝트**: ${env.JOB_NAME}
                            **시작 시간**: ${currentBuild.startTimeInMillis ? new Date(currentBuild.startTimeInMillis).format('yyyy-MM-dd HH:mm:ss') : '알 수 없음'}
                            **종료 시간**: ${currentBuild.endTimeInMillis ? new Date(currentBuild.endTimeInMillis).format('yyyy-MM-dd HH:mm:ss') : '알 수 없음'}
                        """,
                        link: env.BUILD_URL,
                        result: currentBuild.currentResult,
                        title: "${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        webhookURL: "${DISCORD_WEBHOOK_URL}"
                    )
                }
            }
        }
    }
}
