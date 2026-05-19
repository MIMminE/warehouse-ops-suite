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

This starts local PostgreSQL and Redis for API Server development.

- PostgreSQL: `localhost:5432`, database `warehouse_ops`
- Redis: `localhost:6379`

## Run Web And Node Services

```bash
pnpm dev:admin
pnpm dev:pdf
```

The PDF Renderer uses Playwright Chromium for PDF generation. Install the browser runtime once after installing Node packages:

```bash
pnpm --filter @warehouse/pdf-renderer exec playwright install chromium
```

Useful local PDF Renderer checks:

```bash
curl http://127.0.0.1:4050/health
curl 'http://127.0.0.1:4050/samples/invoice?format=html'
curl 'http://127.0.0.1:4050/samples/picking-list?format=html'
```

## Run Kotlin Services

```bash
gradle :services:api-server:bootRun
gradle :apps:print-agent:run
gradle :apps:dps-protocol-agent:run
```

Admin Web calls the API Server through `VITE_API_BASE_URL`. The local default is `http://localhost:8080`.

The default API Server profile is `local` and expects PostgreSQL from Docker Compose. The `smoke` profile uses in-memory H2 so the server process can be checked without Docker.

```bash
gradle :services:api-server:bootRun --args='--spring.profiles.active=smoke'
```

For AWS-oriented runtime settings, see [aws-deployment.md](aws-deployment.md).

## Prepare Flutter PDA App

The initial PDA folder contains the Dart dependencies and app shell. Generate Android native files after installing Flutter:

```bash
cd apps/pda-app
flutter create --platforms=android .
flutter pub get
```

Android APK builds require Android SDK configuration through Android Studio or `ANDROID_HOME`.
