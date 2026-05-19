# Warehouse Ops Suite

Portfolio project for a warehouse operations system inspired by real WMS/LMS domain experience.

This repository is a clean-room redesign. It does not use company source code, internal package names, DTO names, API paths, database schemas, customer names, center names, real logs, or operational configuration values.

## Scope

- Admin Web: warehouse operator back office
- PDA App: field worker scanning workflow
- API Server: WMS domain backend
- Print Agent: local printer bridge
- DPS Protocol Agent: device protocol simulator
- PDF Renderer: invoice and picking list renderer

## Repository Layout

```text
apps/
  admin-web/
  pda-app/
  print-agent/
  dps-protocol-agent/
services/
  api-server/
  pdf-renderer/
packages/
  shared-contracts/
  domain-docs/
infra/
docs/
```

## Tech Stack

- `apps/admin-web`: React, TypeScript, Vite, TanStack Query, Tailwind CSS, React Router
- `apps/pda-app`: Flutter, Dart, Riverpod, Dio, Android target
- `apps/print-agent`: Kotlin, Ktor, Coroutines-ready local HTTP agent
- `apps/dps-protocol-agent`: Kotlin, Ktor WebSocket device protocol simulator
- `services/api-server`: Kotlin, Spring Boot, JPA, PostgreSQL, Flyway
- `services/pdf-renderer`: Node.js, TypeScript, Fastify, Playwright
- `packages/shared-contracts`: TypeScript shared contracts

## Setup

See [docs/setup.md](docs/setup.md).

## Portfolio Report

See [docs/portfolio-report.md](docs/portfolio-report.md) for the current completion report, demo scenario, and remaining production-grade extensions.

For a resume-oriented project write-up with actual Admin Web screenshots, see [docs/resume-portfolio.md](docs/resume-portfolio.md).

For a screen-based user manual and functional specification, see [docs/product-manual.md](docs/product-manual.md).

## First Implementation Target

Outbound wave creation -> DPS picking start -> PDA picking complete -> invoice PDF creation -> Print Agent print request -> print status tracking.
