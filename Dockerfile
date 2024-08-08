# Base image with JDK 17 slim
FROM arm64v8/openjdk:17-ea-jdk-slim

# 가상 디렉터리에 볼륨을 마운트
VOLUME /data

# JAR 파일 복사
COPY build/libs/study-server.jar study-service.jar
# 환경 변수 파일을 yml에 선언된 경로와 같은 경로에 복사
COPY secrets/.env /secrets/.env

# JAR 파일 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "study-service.jar"]