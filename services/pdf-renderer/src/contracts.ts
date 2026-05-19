import { z } from "zod";

const positiveQuantity = z.number().int().positive();

export const invoiceDocumentSchema = z.object({
  invoiceNo: z.string().min(1),
  outboundNo: z.string().min(1),
  clientName: z.string().min(1),
  warehouseName: z.string().min(1),
  carrierName: z.string().min(1),
  trackingNo: z.string().min(1),
  requestedShipDate: z.string().min(1),
  recipient: z.object({
    name: z.string().min(1),
    phone: z.string().min(1),
    address: z.string().min(1),
    memo: z.string().optional(),
  }),
  sender: z.object({
    name: z.string().min(1),
    phone: z.string().min(1),
    address: z.string().min(1),
  }),
  lines: z.array(z.object({
    skuCode: z.string().min(1),
    productName: z.string().min(1),
    optionName: z.string().optional(),
    quantity: positiveQuantity,
    locationCode: z.string().optional(),
  })).min(1),
});

export const pickingListDocumentSchema = z.object({
  waveNo: z.string().min(1),
  clientName: z.string().min(1),
  warehouseName: z.string().min(1),
  zoneName: z.string().min(1),
  generatedAt: z.string().min(1),
  tasks: z.array(z.object({
    taskNo: z.string().min(1),
    invoiceNo: z.string().min(1),
    outboundNo: z.string().min(1),
    skuCode: z.string().min(1),
    productName: z.string().min(1),
    locationCode: z.string().min(1),
    quantity: positiveQuantity,
  })).min(1),
});

export type InvoiceDocument = z.infer<typeof invoiceDocumentSchema>;
export type PickingListDocument = z.infer<typeof pickingListDocumentSchema>;
export type DocumentFormat = "pdf" | "html";
