export type ApiOutboundOrderStatus =
  | "RECEIVED"
  | "ALLOCATED"
  | "WAVE_ASSIGNED"
  | "PICKING"
  | "PACKING"
  | "READY_TO_SHIP"
  | "SHIPPED"
  | "CANCELED";

export type ApiOutboundOrder = {
  id: number;
  clientCompanyId: number;
  clientCompanyName: string;
  warehouseId: number;
  warehouseName: string;
  outboundOrderNo: string;
  externalReferenceNo: string | null;
  intakeSource: "API" | "CSV_UPLOAD" | "EDI_FILE" | "MANUAL";
  status: ApiOutboundOrderStatus;
  receiverName: string;
  requestedShipDate: string | null;
  orderedAt: string | null;
  createdAt: string;
  lineCount: number;
  orderedQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
};

export type ApiOutboundOrderLine = {
  id: number;
  lineNo: number;
  skuId: number;
  skuCode: string;
  skuName: string;
  orderedQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  packedQuantity: number;
};

export type ApiOutboundOrderDetail = {
  order: ApiOutboundOrder;
  lines: ApiOutboundOrderLine[];
};

export type ApiReceivingOrderStatus =
  | "DRAFT"
  | "REQUESTED"
  | "RECEIVING"
  | "PUTAWAY"
  | "COMPLETED"
  | "CANCELED";

export type ApiReceivingOrderLine = {
  id: number;
  lineNo: number;
  skuId: number;
  skuCode: string;
  skuName: string;
  requestedQuantity: number;
  receivedQuantity: number;
  putawayQuantity: number;
};

export type ApiReceivingOrder = {
  id: number;
  receivingNo: string;
  clientCompanyId: number;
  clientCompanyName: string;
  warehouseId: number;
  warehouseName: string;
  status: ApiReceivingOrderStatus;
  supplierName: string | null;
  requestedBy: string;
  requestedQuantity: number;
  receivedQuantity: number;
  putawayQuantity: number;
  createdAt: string;
  lines: ApiReceivingOrderLine[];
};

export type ApiInventory = {
  id: number;
  clientCompanyId: number;
  clientCompanyName: string;
  warehouseId: number;
  warehouseName: string;
  locationId: number;
  locationCode: string;
  locationZone: string | null;
  skuId: number;
  skuCode: string;
  skuName: string;
  availableQuantity: number;
  allocatedQuantity: number;
  holdQuantity: number;
  status: string;
  updatedAt: string;
};

export type ApiClientCompany = {
  id: number;
  code: string;
  name: string;
  active: boolean;
};

export type ApiWarehouse = {
  id: number;
  code: string;
  name: string;
};

export type ApiDashboard = {
  metrics: {
    receivingOrderCount: number;
    receivingRequestedQuantity: number;
    inventoryAvailableQuantity: number;
    inventoryAllocatedQuantity: number;
    inventoryHoldQuantity: number;
    outboundOrderCount: number;
    outboundNeedsAttentionCount: number;
    pickingTaskCount: number;
    pickingPickedQuantity: number;
  };
  issueQueue: Array<{
    label: string;
    count: number;
    description: string;
  }>;
  clientSla: Array<{
    clientCompanyName: string;
    rate: number;
  }>;
  recentReceiving: ApiDashboardListItem[];
  recentOutbound: ApiDashboardListItem[];
  inventoryAlerts: ApiDashboardListItem[];
};

export type ApiDashboardListItem = {
  label: string;
  description: string;
  status: string;
};

export type ApiOutboundWaveStatus =
  | "READY"
  | "ALLOCATED"
  | "PICKING"
  | "COMPLETED"
  | "CANCELED";

export type ApiPickingTaskStatus =
  | "READY"
  | "ASSIGNED"
  | "PICKING"
  | "COMPLETED"
  | "CANCELED";

export type ApiOutboundWavePickingTask = {
  id: number;
  taskNo: string;
  outboundOrderLineId: number | null;
  outboundOrderNo: string | null;
  receiverName: string | null;
  sourceLocationId: number;
  sourceLocationCode: string;
  skuId: number;
  skuCode: string;
  skuName: string;
  status: ApiPickingTaskStatus;
  requestedQuantity: number;
  pickedQuantity: number;
  assignedWorker: string | null;
};

export type ApiOutboundWave = {
  id: number;
  waveNo: string;
  clientCompanyId: number;
  clientCompanyName: string;
  warehouseId: number;
  warehouseName: string;
  status: ApiOutboundWaveStatus;
  requestedBy: string;
  createdAt: string;
  orderCount: number;
  taskCount: number;
  requestedQuantity: number;
  pickedQuantity: number;
  pickingTasks: ApiOutboundWavePickingTask[];
};

export type ApiOutboundWaveCandidateLine = {
  outboundOrderLineId: number;
  outboundOrderNo: string;
  clientCompanyId: number;
  clientCompanyName: string;
  warehouseId: number;
  warehouseName: string;
  receiverName: string;
  skuId: number;
  skuCode: string;
  skuName: string;
  orderedQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  candidateQuantity: number;
  requestedShipDate: string | null;
};

export type OutboundOrderSearchParams = {
  clientCompanyId?: number;
  status?: ApiOutboundOrderStatus;
  requestedShipDateFrom?: string;
  requestedShipDateTo?: string;
};

export type ReceivingOrderSearchParams = {
  clientCompanyId?: number;
  warehouseId?: number;
  status?: ApiReceivingOrderStatus;
  createdFrom?: string;
  createdTo?: string;
};

export type InventorySearchParams = {
  clientCompanyId?: number;
  warehouseId?: number;
  status?: string;
  keyword?: string;
};

export type OutboundWaveSearchParams = {
  clientCompanyId?: number;
  warehouseId?: number;
  status?: ApiOutboundWaveStatus;
  createdFrom?: string;
  createdTo?: string;
};

export type OutboundWaveCandidateParams = {
  clientCompanyId?: number;
  warehouseId?: number;
};

export type CreateOutboundWaveRequest = {
  waveNo: string;
  clientCompanyId: number;
  warehouseId: number;
  requestedBy: string;
  memo?: string;
  outboundOrderLineIds: number[];
};

export type ApiDpsDispatchResponse = {
  waveId: number;
  waveNo: string;
  requestId: string;
  agentResponseType: string;
  cellCount: number;
  sentAt: string;
};

export type ApiPrintJob = {
  id: number;
  jobNo: string;
  outboundWaveId: number | null;
  outboundWaveNo: string | null;
  pickingTaskId: number | null;
  pickingTaskNo: string | null;
  status: "REQUESTED" | "QUEUED" | "PRINTING" | "PRINTED" | "FAILED" | "CANCELED";
  documentType: "INVOICE" | "PICKING_LIST";
  printerName: string | null;
  failureReason: string | null;
  createdAt: string;
  updatedAt: string;
};
