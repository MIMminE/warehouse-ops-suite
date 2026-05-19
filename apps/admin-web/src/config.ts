export const appConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
  dpsWebSocketUrl: import.meta.env.VITE_DPS_WEBSOCKET_URL ?? "ws://localhost:4030/ws/dps",
  dpsAgentBasePath: import.meta.env.VITE_DPS_AGENT_BASE_PATH ?? "/dps-agent",
  defaultPickingListPrinter: import.meta.env.VITE_DEFAULT_PICKING_LIST_PRINTER ?? "HP-LaserJet-PICK",
  serviceEndpoints: {
    apiServer: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
    pdfRenderer: import.meta.env.VITE_PDF_RENDERER_URL ?? "http://localhost:4050",
    dpsAgent: import.meta.env.VITE_DPS_WEBSOCKET_URL ?? "ws://localhost:4030/ws/dps",
    printAgent: import.meta.env.VITE_PRINT_AGENT_URL ?? "http://localhost:4020",
  },
} as const;
