# Architecture

## Applications

- `apps/admin-web`: React back office for warehouse operators.
- `apps/pda-app`: Flutter Android app for barcode-based field work.
- `apps/print-agent`: Kotlin/Ktor local HTTP agent for printer integration.
- `apps/dps-protocol-agent`: Kotlin/Ktor WebSocket agent that simulates DPS hardware.

## Services

- `services/api-server`: Kotlin/Spring Boot WMS backend.
- `services/pdf-renderer`: Node.js/TypeScript PDF rendering service.

## Shared Packages

- `packages/shared-contracts`: TypeScript API event and DTO contracts for web-facing packages.
- `packages/domain-docs`: domain notes and decision records.

## Communication

- Admin Web -> API Server: HTTP REST
- PDA App -> API Server: HTTP REST
- API Server -> DPS Protocol Agent: WebSocket
- API Server -> PDF Renderer: HTTP REST
- API Server/Admin Web -> Print Agent: local HTTP

