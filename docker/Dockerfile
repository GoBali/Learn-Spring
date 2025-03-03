# 1. Gradle 빌드를 위한 Java 이미지 (빌드 단계)
FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /app

# 2. 프로젝트 소스를 도커 이미지로 복사
COPY .. .

# 3. Gradle 빌드 실행 (JAR 생성)
RUN gradle clean build --no-daemon

# 4. Runtime 실행을 위한 가벼운 JDK 이미지 (런타임 단계)
FROM openjdk:21-jdk-slim
WORKDIR /app

# 5. 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 6. Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]