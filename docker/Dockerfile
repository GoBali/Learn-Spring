FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /app

COPY ../build.gradle.kts ../settings.gradle.kts ./
RUN gradle clean build --no-daemon --refresh-dependencies --exclude-task test || true

# 이제 나머지 소스 코드를 복사
COPY .. .
RUN gradle clean build --no-daemon

FROM openjdk:21-jdk-slim

RUN groupadd --system spring && useradd --system spring -g spring
USER spring:spring

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
