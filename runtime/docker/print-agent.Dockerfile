FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon :apps:print-agent:jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/apps/print-agent/build/libs/*.jar app.jar
EXPOSE 4020
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
