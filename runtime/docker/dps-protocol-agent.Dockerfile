FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon :apps:dps-protocol-agent:jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/apps/dps-protocol-agent/build/libs/*.jar app.jar
EXPOSE 4030
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
