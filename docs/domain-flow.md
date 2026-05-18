# Domain Flow

## First Slice: Outbound Picking And Printing

```mermaid
sequenceDiagram
    participant Admin as Admin Web
    participant API as API Server
    participant DPS as DPS Protocol Agent
    participant PDA as PDA App
    participant PDF as PDF Renderer
    participant Print as Print Agent

    Admin->>API: Create outbound wave
    API->>DPS: Start picking job
    DPS-->>API: Cell light result
    PDA->>API: Confirm picked quantity
    API->>PDF: Render invoice PDF
    PDF-->>API: PDF file reference
    API->>Print: Request print
    Print-->>API: Print queue status
    API-->>Admin: Picking and print status
```

## State Examples

- Picking: `READY -> ASSIGNED -> PICKING -> COMPLETED -> CANCELED`
- Print job: `REQUESTED -> QUEUED -> PRINTING -> PRINTED -> FAILED`
- DPS cell: `IDLE -> LIGHT_ON -> CONFIRMED -> LIGHT_OFF -> ERROR`

