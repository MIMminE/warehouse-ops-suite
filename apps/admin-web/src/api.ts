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

export type OutboundOrderSearchParams = {
  clientCompanyId?: number;
  status?: ApiOutboundOrderStatus;
  requestedShipDateFrom?: string;
  requestedShipDateTo?: string;
};

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const request = async <T>(path: string): Promise<T> => {
  const response = await fetch(`${apiBaseUrl}${path}`);

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
};

const toQueryString = (params: Record<string, string | number | undefined>) => {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      query.set(key, String(value));
    }
  });
  const queryString = query.toString();
  return queryString ? `?${queryString}` : "";
};

export const warehouseApi = {
  getHealth: () => request<{ status: string }>("/actuator/health"),
  searchOutboundOrders: (params: OutboundOrderSearchParams) =>
    request<ApiOutboundOrder[]>(`/api/outbound-orders${toQueryString(params)}`),
  getOutboundOrderDetail: (orderId: number) =>
    request<ApiOutboundOrderDetail>(`/api/outbound-orders/${orderId}`),
};
