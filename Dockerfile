FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY gradlew build.gradle settings.gradle ./
COPY gradle/wrapper/ gradle/wrapper/
RUN chmod +x gradlew
COPY src/ src/
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
