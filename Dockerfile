# 1단계: Spring Boot 빌드
FROM gradle:9.7.1-jdk17 AS builder

WORKDIR /app

COPY . .

RUN ./gradlew bootJar --no-daemon


# 2단계: 실제 실행 이미지
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]