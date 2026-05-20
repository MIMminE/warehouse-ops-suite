FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon :apps:dps-protocol-agent:installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/apps/dps-protocol-agent/build/install/dps-protocol-agent ./
EXPOSE 4030
ENTRYPOINT ["/app/bin/dps-protocol-agent"]
