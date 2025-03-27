FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /app

# 의존성 캐싱을 위한 그래들 파일 복사
COPY ./build.gradle.kts ./settings.gradle.kts ./
# 캐싱을 위해 의존성만 먼저 다운로드
RUN gradle dependencies --no-daemon

# 소스 코드 복사 후 빌드
COPY ./src ./src

# 개발 환경에서는 테스트를 건너뛰고 빠른 빌드
RUN gradle bootJar --no-daemon -x test

# 올바른 이미지 태그 사용
FROM eclipse-temurin:21-jre

#RUN groupadd --system spring && useradd --system spring -g spring
#USER spring:spring

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# 개발용 JVM 옵션
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms256m -Xmx512m -Dspring.devtools.restart.enabled=true"

# 원격 디버깅 포트 노출
EXPOSE 8080 5005

# 디버깅 모드로 실행
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar"]