# Local Setup

## Required Tools

- Node.js 22+
- pnpm 9+
- JDK 21+
- Gradle 8+
- Flutter 3.27+ for `apps/pda-app`
- Docker Desktop or compatible Docker runtime

## Install Node Packages

```bash
cd warehouse-ops-suite
pnpm install
```

## Start Database

```bash
docker compose -f infra/docker-compose.yml up -d
```

## Run Web And Node Services

```bash
pnpm dev:admin
pnpm dev:pdf
```

## Run Kotlin Services

```bash
gradle :services:api-server:bootRun --args='--spring.profiles.active=smoke'
gradle :apps:print-agent:run
gradle :apps:dps-protocol-agent:run
```

The default API Server profile expects PostgreSQL from Docker Compose. The `smoke` profile uses in-memory H2 so the server process can be checked without Docker.

## Prepare Flutter PDA App

The initial PDA folder contains the Dart dependencies and app shell. Generate Android native files after installing Flutter:

```bash
cd apps/pda-app
flutter create --platforms=android .
flutter pub get
```

Android APK builds require Android SDK configuration through Android Studio or `ANDROID_HOME`.
