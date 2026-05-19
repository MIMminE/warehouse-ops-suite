import cors from "@fastify/cors";
import Fastify, { type FastifyReply } from "fastify";
import { ZodError } from "zod";
import {
  type DocumentFormat,
  invoiceDocumentSchema,
  pickingListDocumentSchema,
} from "./contracts.js";
import { closeBrowser, renderPdf } from "./pdf.js";
import { sampleInvoice, samplePickingList } from "./samples.js";
import { renderInvoiceHtml, renderPickingListHtml } from "./templates.js";

const app = Fastify({ logger: true });

await app.register(cors, {
  origin: true,
});

app.setErrorHandler((error, _request, reply) => {
  if (error instanceof ZodError) {
    return reply.status(400).send({
      code: "INVALID_DOCUMENT_REQUEST",
      message: "문서 렌더링 요청 형식이 올바르지 않습니다.",
      details: error.flatten(),
    });
  }

  app.log.error(error);
  return reply.status(500).send({
    code: "PDF_RENDER_FAILED",
    message: "문서 렌더링 중 오류가 발생했습니다.",
  });
});

const formatOf = (format: unknown): DocumentFormat =>
  format === "html" ? "html" : "pdf";

const sendDocument = async (
  reply: FastifyReply,
  html: string,
  filename: string,
  format: DocumentFormat,
) => {
  if (format === "html") {
    return reply.type("text/html; charset=utf-8").send(html);
  }

  const pdf = await renderPdf(html);
  return reply
    .type("application/pdf")
    .header("content-disposition", `inline; filename="${filename}.pdf"`)
    .send(pdf);
};

app.get("/health", async () => ({
  status: "ok",
  service: "pdf-renderer",
  documents: ["invoice", "picking-list"],
}));

app.get<{ Querystring: { format?: string } }>("/samples/invoice", async (request, reply) => {
  const html = renderInvoiceHtml(sampleInvoice);
  return sendDocument(reply, html, sampleInvoice.invoiceNo, formatOf(request.query.format));
});

app.get<{ Querystring: { format?: string } }>("/samples/picking-list", async (request, reply) => {
  const html = renderPickingListHtml(samplePickingList);
  return sendDocument(reply, html, samplePickingList.waveNo, formatOf(request.query.format));
});

app.post<{ Querystring: { format?: string } }>("/documents/invoice", async (request, reply) => {
  const document = invoiceDocumentSchema.parse(request.body);
  const html = renderInvoiceHtml(document);
  return sendDocument(reply, html, document.invoiceNo, formatOf(request.query.format));
});

app.post<{ Querystring: { format?: string } }>("/documents/picking-list", async (request, reply) => {
  const document = pickingListDocumentSchema.parse(request.body);
  const html = renderPickingListHtml(document);
  return sendDocument(reply, html, document.waveNo, formatOf(request.query.format));
});

const port = Number(process.env.PORT ?? 4050);
const host = process.env.HOST ?? "127.0.0.1";
await app.listen({ host, port });

const shutdown = async () => {
  await closeBrowser();
  await app.close();
};

process.on("SIGTERM", () => {
  void shutdown();
});

process.on("SIGINT", () => {
  void shutdown();
});
