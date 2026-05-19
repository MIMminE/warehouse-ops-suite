import { appConfig } from "./config";
import type {
  ApiClientCompany,
  ApiDashboard,
  ApiDpsDispatchResponse,
  ApiInventory,
  ApiOutboundOrder,
  ApiOutboundOrderDetail,
  ApiOutboundWave,
  ApiOutboundWaveCandidateLine,
  ApiPrintJob,
  ApiReceivingOrder,
  ApiWarehouse,
  CreateOutboundWaveRequest,
  InventorySearchParams,
  OutboundOrderSearchParams,
  OutboundWaveCandidateParams,
  OutboundWaveSearchParams,
  ReceivingOrderSearchParams,
} from "./api-types";

type QueryValue = string | number | undefined;

const request = async <T>(path: string, init?: RequestInit): Promise<T> => {
  const response = await fetch(`${appConfig.apiBaseUrl}${path}`, {
    headers: init?.body ? { "content-type": "application/json", ...init.headers } : init?.headers,
    ...init,
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
};

const toQueryString = (params: Record<string, QueryValue>) => {
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
  getDashboard: () => request<ApiDashboard>("/api/dashboard"),
  getClientCompanies: () => request<ApiClientCompany[]>("/api/client-companies"),
  getWarehouses: () => request<ApiWarehouse[]>("/api/warehouses"),
  searchOutboundOrders: (params: OutboundOrderSearchParams) =>
    request<ApiOutboundOrder[]>(`/api/outbound-orders${toQueryString(params)}`),
  getOutboundOrderDetail: (orderId: number) =>
    request<ApiOutboundOrderDetail>(`/api/outbound-orders/${orderId}`),
  searchReceivingOrders: (params: ReceivingOrderSearchParams) =>
    request<ApiReceivingOrder[]>(`/api/receiving-orders${toQueryString(params)}`),
  searchInventories: (params: InventorySearchParams) =>
    request<ApiInventory[]>(`/api/inventories${toQueryString(params)}`),
  searchOutboundWaves: (params: OutboundWaveSearchParams) =>
    request<ApiOutboundWave[]>(`/api/outbound-waves${toQueryString(params)}`),
  getOutboundWaveDetail: (waveId: number) =>
    request<ApiOutboundWave>(`/api/outbound-waves/${waveId}`),
  getOutboundWaveCandidates: (params: OutboundWaveCandidateParams) =>
    request<ApiOutboundWaveCandidateLine[]>(`/api/outbound-waves/candidates${toQueryString(params)}`),
  createOutboundWave: (payload: CreateOutboundWaveRequest) =>
    request<ApiOutboundWave>("/api/outbound-waves", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  dispatchOutboundWaveToDps: (waveId: number) =>
    request<ApiDpsDispatchResponse>(`/api/outbound-waves/${waveId}/dispatch-dps`, {
      method: "POST",
    }),
  getWavePrintJobs: (waveId: number) =>
    request<ApiPrintJob[]>(`/api/outbound-waves/${waveId}/print-jobs`),
  requestPickingListPrint: (
    waveId: number,
    printerName = appConfig.defaultPickingListPrinter,
  ) =>
    request<ApiPrintJob>(`/api/outbound-waves/${waveId}/print-jobs/picking-list`, {
      method: "POST",
      body: JSON.stringify({ printerName }),
    }),
};
