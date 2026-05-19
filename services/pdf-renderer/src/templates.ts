import type { InvoiceDocument, PickingListDocument } from "./contracts.js";

const escapeHtml = (value: string) =>
  value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");

const number = (value: number) => value.toLocaleString("ko-KR");

const baseDocument = (title: string, body: string) => `<!doctype html>
<html lang="ko">
  <head>
    <meta charset="utf-8" />
    <title>${escapeHtml(title)}</title>
    <style>
      @page { size: A4; margin: 16mm; }
      * { box-sizing: border-box; }
      body {
        margin: 0;
        color: #162029;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        font-size: 12px;
      }
      h1, h2, p { margin: 0; }
      .document { display: flex; flex-direction: column; gap: 18px; }
      .header { display: flex; justify-content: space-between; gap: 20px; border-bottom: 2px solid #162029; padding-bottom: 14px; }
      .title { font-size: 25px; font-weight: 800; letter-spacing: 0; }
      .subtitle { margin-top: 7px; color: #5f6b76; font-size: 11px; }
      .barcode { border: 1px solid #162029; min-width: 210px; padding: 10px; text-align: center; }
      .barcode-value { margin-top: 8px; font-size: 15px; font-weight: 800; }
      .bars { height: 46px; background: repeating-linear-gradient(90deg, #162029 0 2px, #fff 2px 5px, #162029 5px 6px, #fff 6px 10px); }
      .grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
      .panel { border: 1px solid #d8dee5; padding: 12px; }
      .panel-title { color: #56616c; font-size: 11px; font-weight: 700; margin-bottom: 8px; }
      .kv { display: grid; grid-template-columns: 96px 1fr; gap: 6px 10px; }
      .key { color: #6a7682; }
      .value { font-weight: 700; }
      table { border-collapse: collapse; width: 100%; }
      th, td { border: 1px solid #d8dee5; padding: 8px; text-align: left; vertical-align: top; }
      th { background: #f1f4f7; color: #38444f; font-size: 11px; }
      td.numeric { text-align: right; font-weight: 700; }
      .memo { min-height: 54px; line-height: 1.5; }
      .footer { color: #6a7682; display: flex; justify-content: space-between; border-top: 1px solid #d8dee5; padding-top: 10px; }
    </style>
  </head>
  <body>
    <main class="document">${body}</main>
  </body>
</html>`;

export const renderInvoiceHtml = (document: InvoiceDocument) => {
  const totalQuantity = document.lines.reduce((sum, line) => sum + line.quantity, 0);
  const lineRows = document.lines.map((line, index) => `
    <tr>
      <td>${index + 1}</td>
      <td>
        <strong>${escapeHtml(line.productName)}</strong>
        ${line.optionName ? `<div>${escapeHtml(line.optionName)}</div>` : ""}
      </td>
      <td>${escapeHtml(line.skuCode)}</td>
      <td>${escapeHtml(line.locationCode ?? "-")}</td>
      <td class="numeric">${number(line.quantity)}</td>
    </tr>
  `).join("");

  return baseDocument("송장", `
    <section class="header">
      <div>
        <h1 class="title">송장</h1>
        <p class="subtitle">${escapeHtml(document.clientName)} / ${escapeHtml(document.warehouseName)}</p>
      </div>
      <div class="barcode">
        <div class="bars"></div>
        <div class="barcode-value">${escapeHtml(document.trackingNo)}</div>
      </div>
    </section>
    <section class="grid">
      <div class="panel">
        <h2 class="panel-title">출고 정보</h2>
        <div class="kv">
          <span class="key">송장번호</span><span class="value">${escapeHtml(document.invoiceNo)}</span>
          <span class="key">출고지시</span><span class="value">${escapeHtml(document.outboundNo)}</span>
          <span class="key">택배사</span><span class="value">${escapeHtml(document.carrierName)}</span>
          <span class="key">출고요청일</span><span class="value">${escapeHtml(document.requestedShipDate)}</span>
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">수량 요약</h2>
        <div class="kv">
          <span class="key">상품 라인</span><span class="value">${number(document.lines.length)}개</span>
          <span class="key">총 수량</span><span class="value">${number(totalQuantity)}개</span>
        </div>
      </div>
    </section>
    <section class="grid">
      <div class="panel">
        <h2 class="panel-title">받는 분</h2>
        <div class="kv">
          <span class="key">이름</span><span class="value">${escapeHtml(document.recipient.name)}</span>
          <span class="key">연락처</span><span>${escapeHtml(document.recipient.phone)}</span>
          <span class="key">주소</span><span>${escapeHtml(document.recipient.address)}</span>
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">보내는 곳</h2>
        <div class="kv">
          <span class="key">이름</span><span class="value">${escapeHtml(document.sender.name)}</span>
          <span class="key">연락처</span><span>${escapeHtml(document.sender.phone)}</span>
          <span class="key">주소</span><span>${escapeHtml(document.sender.address)}</span>
        </div>
      </div>
    </section>
    <section>
      <table>
        <thead>
          <tr>
            <th>No</th>
            <th>상품</th>
            <th>SKU</th>
            <th>로케이션</th>
            <th>수량</th>
          </tr>
        </thead>
        <tbody>${lineRows}</tbody>
      </table>
    </section>
    <section class="panel memo">
      <h2 class="panel-title">배송 메모</h2>
      ${escapeHtml(document.recipient.memo ?? "-")}
    </section>
    <footer class="footer">
      <span>Warehouse Ops Suite PDF Renderer</span>
      <span>${escapeHtml(document.invoiceNo)}</span>
    </footer>
  `);
};

export const renderPickingListHtml = (document: PickingListDocument) => {
  const totalQuantity = document.tasks.reduce((sum, task) => sum + task.quantity, 0);
  const taskRows = document.tasks.map((task, index) => `
    <tr>
      <td>${index + 1}</td>
      <td>${escapeHtml(task.locationCode)}</td>
      <td>
        <strong>${escapeHtml(task.productName)}</strong>
        <div>${escapeHtml(task.skuCode)}</div>
      </td>
      <td>${escapeHtml(task.invoiceNo)}</td>
      <td>${escapeHtml(task.outboundNo)}</td>
      <td class="numeric">${number(task.quantity)}</td>
      <td>${escapeHtml(task.taskNo)}</td>
    </tr>
  `).join("");

  return baseDocument("피킹리스트", `
    <section class="header">
      <div>
        <h1 class="title">피킹리스트</h1>
        <p class="subtitle">${escapeHtml(document.clientName)} / ${escapeHtml(document.warehouseName)} / ${escapeHtml(document.zoneName)}</p>
      </div>
      <div class="barcode">
        <div class="bars"></div>
        <div class="barcode-value">${escapeHtml(document.waveNo)}</div>
      </div>
    </section>
    <section class="grid">
      <div class="panel">
        <h2 class="panel-title">웨이브 정보</h2>
        <div class="kv">
          <span class="key">웨이브</span><span class="value">${escapeHtml(document.waveNo)}</span>
          <span class="key">생성일시</span><span>${escapeHtml(document.generatedAt)}</span>
          <span class="key">작업 구역</span><span>${escapeHtml(document.zoneName)}</span>
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">작업 요약</h2>
        <div class="kv">
          <span class="key">작업 라인</span><span class="value">${number(document.tasks.length)}개</span>
          <span class="key">총 피킹 수량</span><span class="value">${number(totalQuantity)}개</span>
        </div>
      </div>
    </section>
    <section>
      <table>
        <thead>
          <tr>
            <th>No</th>
            <th>로케이션</th>
            <th>상품</th>
            <th>송장</th>
            <th>출고지시</th>
            <th>수량</th>
            <th>작업번호</th>
          </tr>
        </thead>
        <tbody>${taskRows}</tbody>
      </table>
    </section>
    <footer class="footer">
      <span>Warehouse Ops Suite PDF Renderer</span>
      <span>${escapeHtml(document.waveNo)}</span>
    </footer>
  `);
};
