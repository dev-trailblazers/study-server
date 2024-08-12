pipeline {
    agent any

    options {
        timeout(time: 1, unit: 'HOURS')
        skipStagesAfterUnstable()
    }

    environment {
        IMAGE_NAME = 'monolithic/study-service'
        IMAGE_TAG = 'latest'
        CONTAINER_NAME = 'study-service'
        DEPLOY_USER = 'pi'
        DEPLOY_IP = credentials('raspberry-pi-ip')
        SSH_PORT = credentials('raspberry-pi-port')
        DOCKER_REGISTRY_URL = credentials('docker-registry-url')
        MY_EMAIL = credentials('my-email')
    }

    stages {
        stage('Git Clone & Submodule Init') {
            steps {
                checkout scmGit(
                    branches: [[name: 'main']],
                    extensions: [submodule(parentCredentials: true, reference: '', recursiveSubmodules: true, trackingSubmodules: true)],
                    userRemoteConfigs: [[
                        url: 'https://github.com/dev-trailblazers/study-server.git',
                        credentialsId: 'github_access_token'
                    ]]
                )
            }
        }
        stage('Gradle Build') {
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
                    sh "docker build --platform linux/amd64 -t $DOCKER_REGISTRY_URL/$IMAGE_NAME:$IMAGE_TAG ."
                }
            }
        }
        stage('Docker Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-registry', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh '''
                            echo $DOCKER_PASSWORD | docker login $DOCKER_REGISTRY_URL --username $DOCKER_USERNAME --password-stdin
                            docker push $DOCKER_REGISTRY_URL/$IMAGE_NAME:$IMAGE_TAG
                        '''
                    }
                }
            }
        }
        stage('Container Deploy') {
            steps {
                sshagent(credentials: ['jenkins-ssh']) {
                    withCredentials([usernamePassword(credentialsId: 'docker-registry', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        /*
                            sh에서 '''로 멀티 라인 명령을 실행하면 sh 문법을 사용해서 $변수명으로 변수를 바인딩한다.
                            """를 사용하면 groovy 문법을 사용해서 ${변수명}으로 변수를 바인딩한다.
                            sh에서 ssh로 해당 환경에 접속해서 아래 명령을 실행할 때 '''를 사용하면 ssh 서버에 있는 변수를 찾기 때문에 """를 사용한다.
                        */
                        sh """
                            ssh -p ${SSH_PORT} -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_IP} '
                                echo ${DOCKER_PASSWORD} | sudo docker login ${DOCKER_REGISTRY_URL} --username ${DOCKER_USERNAME} --password-stdin
                                sudo docker stop ${CONTAINER_NAME} || true
                                sudo docker rm ${CONTAINER_NAME} || true
                                sudo docker pull ${DOCKER_REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG}
                                sudo docker run -d -p 56789:56789 --restart always --name ${CONTAINER_NAME} ${DOCKER_REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG}
                            '
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK_URL')]) {
                    def startTime = currentBuild.startTimeInMillis ? new Date(currentBuild.startTimeInMillis).format('yyyy-MM-dd HH:mm:ss') : '알 수 없음'
                    def endTime = currentBuild.getTimeInMillis() ? new Date(currentBuild.getTimeInMillis()).format('yyyy-MM-dd HH:mm:ss') : '알 수 없음'

                    discordSend(
                        description: """
                            **Study Service Monolithic CICD #${env.BUILD_NUMBER}**
                            **Status**: ${currentBuild.currentResult}
                            **프로젝트**: ${env.JOB_NAME}
                            **시작 시간**: ${startTime}
                            **종료 시간**: ${endTime}
                        """,
                        link: env.BUILD_URL,
                        result: 'SUCCESS',
                        title: "${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        webhookURL: "${DISCORD_WEBHOOK_URL}"
                    )
                }
            }
        }
        failure {
            script {
                def startTime = currentBuild.startTimeInMillis ? new Date(currentBuild.startTimeInMillis).format('yyyy-MM-dd HH:mm:ss') : '알 수 없음'
                def endTime = currentBuild.getTimeInMillis() ? new Date(currentBuild.getTimeInMillis()).format('yyyy-MM-dd HH:mm:ss') : '알 수 없음'
                def recipient = "${MY_EMAIL}"
                def subject = "Jenkins Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                def body = """
                <h2>Jenkins Deployment Failed</h2>
                <ul>
                    <li><strong>Job Name:</strong> ${env.JOB_NAME}</li>
                    <li><strong>Build Number:</strong> ${env.BUILD_NUMBER}</li>
                    <li><strong>Status:</strong> ${currentBuild.currentResult}</li>
                    <li><strong>Start Time:</strong> ${startTime}</li>
                    <li><strong>End Time:</strong> ${endTime}</li>
                    <li><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
                </ul>
                <p>Please check the Jenkins console output for more details.</p>
                """
                emailext(
                    to: recipient,
                    subject: subject,
                    body: body,
                    mimeType: 'text/html'
                )
            }
        }
    }
}
