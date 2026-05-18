# API Contract Draft

## API Server

- `POST /api/outbound-waves`
- `GET /api/outbound-waves/{waveId}`
- `POST /api/picking-tasks/{taskId}/confirm`
- `POST /api/print-jobs`
- `GET /api/print-jobs/{printJobId}`
- `GET /api/agents`

## Print Agent

- `GET /local/printers`
- `POST /local/print-jobs`
- `GET /local/print-jobs/{printJobId}`

## DPS Protocol Agent

- `WS /ws/dps`

Message types:

- `PICKING_JOB_STARTED`
- `CELL_LIGHT_REQUESTED`
- `CELL_LIGHT_FAILED`
- `PICKING_CONFIRMED`
- `PICKING_JOB_CANCELED`

