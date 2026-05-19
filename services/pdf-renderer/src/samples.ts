import type { InvoiceDocument, PickingListDocument } from "./contracts.js";

export const sampleInvoice: InvoiceDocument = {
  invoiceNo: "INV-260519-0007",
  outboundNo: "OUT-20260519-0804",
  clientName: "A 고객사",
  warehouseName: "Nuts Fulfillment Center",
  carrierName: "우체국택배",
  trackingNo: "6862-0519-0007",
  requestedShipDate: "2026-05-19",
  recipient: {
    name: "최도윤",
    phone: "010-1234-5678",
    address: "서울특별시 강남구 테헤란로 123",
    memo: "부재 시 문 앞에 놓아주세요.",
  },
  sender: {
    name: "Nuts 물류센터",
    phone: "02-0000-1000",
    address: "경기도 성남시 분당구 물류로 42",
  },
  lines: [
    {
      skuCode: "SKU-1024",
      productName: "Slim Bottle / Clear",
      optionName: "500ml",
      quantity: 2,
      locationCode: "C-04-05",
    },
  ],
};

export const samplePickingList: PickingListDocument = {
  waveNo: "WAVE-0518-PM-01",
  clientName: "A 고객사",
  warehouseName: "Nuts Fulfillment Center",
  zoneName: "DPS Zone",
  generatedAt: "2026-05-19 14:30",
  tasks: [
    {
      taskNo: "PICK-260519-0001",
      invoiceNo: "INV-260519-0006",
      outboundNo: "OUT-20260518-0803",
      skuCode: "SKU-7780",
      productName: "Pouch Set / Gray",
      locationCode: "D-01-02",
      quantity: 6,
    },
    {
      taskNo: "PICK-260519-0002",
      invoiceNo: "INV-260519-0007",
      outboundNo: "OUT-20260519-0804",
      skuCode: "SKU-1024",
      productName: "Slim Bottle / Clear",
      locationCode: "C-04-05",
      quantity: 2,
    },
  ],
};
