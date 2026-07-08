FROM eclipse-temurin:23-jdk
WORKDIR /home
COPY build/libs/BridgeTournament-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]