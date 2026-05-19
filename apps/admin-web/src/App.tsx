import {
  Activity,
  Boxes,
  CalendarDays,
  CheckCircle2,
  ChevronRight,
  ClipboardList,
  Download,
  Filter,
  LayoutDashboard,
  PackageCheck,
  PlugZap,
  RadioTower,
  RefreshCcw,
  Search,
  Send,
  SlidersHorizontal,
  Truck,
  Warehouse,
  X,
} from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import type { ReactNode } from "react";

type AdminSection =
  | "dashboard"
  | "receiving"
  | "inventory"
  | "outbound"
  | "picking"
  | "dps"
  | "agents";

type OperationFilters = {
  client: string;
  warehouse: string;
  status: string;
  fromDate: string;
  toDate: string;
  keyword: string;
};

type DpsEvent = {
  type: string;
  requestId?: string | null;
  payload?: unknown;
  sentAt?: string;
};

type DpsCellState = {
  cellCode: string;
  skuCode: string;
  quantity: number;
  state: "IDLE" | "LIGHT_ON" | "COMPLETED" | "FAILED";
};

type DpsSimulatorSnapshot = {
  status: {
    activeBatchId: string | null;
    cellCount: number;
    litCellCount: number;
    completedCellCount: number;
    failedCellCount: number;
  };
  cells: DpsCellState[];
};

type ReceivingRow = {
  no: string;
  client: string;
  warehouse: string;
  sku: string;
  product: string;
  supplier: string;
  expectedDate: string;
  requested: number;
  received: number;
  putaway: number;
  status: string;
  worker: string;
  device: string;
  lastScannedAt: string;
};

type ReceivingSkuDetail = {
  receivingNo: string;
  sku: string;
  product: string;
  requested: number;
  inspected: number;
  putaway: number;
  damaged: number;
  shortage: number;
  targetLocations: string[];
};

type ReceivingScanEvent = {
  receivingNo: string;
  time: string;
  worker: string;
  device: string;
  event: string;
  barcode: string;
  result: string;
};

type InventoryRow = {
  sku: string;
  name: string;
  client: string;
  warehouse: string;
  location: string;
  lot: string;
  available: number;
  allocated: number;
  hold: number;
  lastMovedAt: string;
  status: string;
};

type LocationCell = {
  code: string;
  zone: string;
  bay: string;
  level: string;
  capacity: number;
  used: number;
  status: "정상" | "주의" | "보류" | "빈 로케이션";
  handlingType: "피킹" | "보관" | "검수" | "반품";
};

type OutboundRow = {
  no: string;
  client: string;
  warehouse: string;
  channel: string;
  recipient: string;
  requestedAt: string;
  lines: number;
  allocated: number;
  picked: number;
  status: string;
};

type PickingRow = {
  wave: string;
  client: string;
  warehouse: string;
  zone: string;
  startedAt: string;
  orders: number;
  tasks: number;
  picked: number;
  status: string;
};

type InvoiceRow = {
  outboundNo: string;
  invoiceNo: string;
  sku: string;
  product: string;
  quantity: number;
  allocated: number;
  picked: number;
  carrier: string;
  printStatus: string;
};

type WaveInvoiceRow = {
  wave: string;
  invoiceNo: string;
  outboundNo: string;
  client: string;
  recipient: string;
  sku: string;
  product: string;
  location: string;
  quantity: number;
  picked: number;
  worker: string;
  device: string;
  status: string;
};

const navItems: Array<{ id: AdminSection; label: string; icon: typeof LayoutDashboard }> = [
  { id: "dashboard", label: "운영 현황", icon: LayoutDashboard },
  { id: "receiving", label: "입고/적치", icon: PackageCheck },
  { id: "inventory", label: "재고", icon: Boxes },
  { id: "outbound", label: "출고 지시", icon: Truck },
  { id: "picking", label: "피킹 웨이브", icon: ClipboardList },
  { id: "dps", label: "DPS 모니터", icon: RadioTower },
  { id: "agents", label: "시스템 연결", icon: PlugZap },
];

const defaultFilters: OperationFilters = {
  client: "전체",
  warehouse: "전체",
  status: "전체",
  fromDate: "2026-05-18",
  toDate: "2026-05-19",
  keyword: "",
};

const clientOptions = ["전체", "A 고객사", "B 고객사", "C 고객사"];
const warehouseOptions = ["전체", "수도권 1센터", "부산 2센터"];

const receivingRows: ReceivingRow[] = [
  {
    no: "RCV-20260518-001",
    client: "A 고객사",
    warehouse: "수도권 1센터",
    sku: "SKU-4012",
    product: "Basic Tee / Black",
    supplier: "남양주 공급처",
    expectedDate: "2026-05-18",
    requested: 120,
    received: 96,
    putaway: 72,
    status: "검수중",
    worker: "한지훈",
    device: "PDA-01",
    lastScannedAt: "10:42",
  },
  {
    no: "RCV-20260518-002",
    client: "B 고객사",
    warehouse: "수도권 1센터",
    sku: "SKU-8801",
    product: "Daily Cap / Navy",
    supplier: "인천 공급처",
    expectedDate: "2026-05-18",
    requested: 80,
    received: 80,
    putaway: 80,
    status: "적치완료",
    worker: "오세린",
    device: "PDA-03",
    lastScannedAt: "11:08",
  },
  {
    no: "RCV-20260518-003",
    client: "A 고객사",
    warehouse: "부산 2센터",
    sku: "SKU-1024",
    product: "Slim Bottle / Clear",
    supplier: "김해 공급처",
    expectedDate: "2026-05-19",
    requested: 240,
    received: 210,
    putaway: 120,
    status: "적치중",
    worker: "김도현",
    device: "PDA-02",
    lastScannedAt: "13:24",
  },
  {
    no: "RCV-20260519-004",
    client: "C 고객사",
    warehouse: "수도권 1센터",
    sku: "SKU-7780",
    product: "Pouch Set / Gray",
    supplier: "성남 공급처",
    expectedDate: "2026-05-19",
    requested: 160,
    received: 0,
    putaway: 0,
    status: "입고예정",
    worker: "-",
    device: "-",
    lastScannedAt: "-",
  },
];

const receivingSkuDetails: ReceivingSkuDetail[] = [
  { receivingNo: "RCV-20260518-001", sku: "SKU-4012", product: "Basic Tee / Black", requested: 120, inspected: 96, putaway: 72, damaged: 2, shortage: 24, targetLocations: ["A-01-03", "A-01-02"] },
  { receivingNo: "RCV-20260518-002", sku: "SKU-8801", product: "Daily Cap / Navy", requested: 80, inspected: 80, putaway: 80, damaged: 0, shortage: 0, targetLocations: ["B-02-01"] },
  { receivingNo: "RCV-20260518-003", sku: "SKU-1024", product: "Slim Bottle / Clear", requested: 240, inspected: 210, putaway: 120, damaged: 4, shortage: 30, targetLocations: ["C-04-05", "C-03-01"] },
  { receivingNo: "RCV-20260519-004", sku: "SKU-7780", product: "Pouch Set / Gray", requested: 160, inspected: 0, putaway: 0, damaged: 0, shortage: 0, targetLocations: ["D-01-02"] },
];

const receivingScanEvents: ReceivingScanEvent[] = [
  { receivingNo: "RCV-20260518-001", time: "10:12", worker: "한지훈", device: "PDA-01", event: "입고번호 스캔", barcode: "RCV-20260518-001", result: "성공" },
  { receivingNo: "RCV-20260518-001", time: "10:19", worker: "한지훈", device: "PDA-01", event: "상품 검수", barcode: "SKU-4012", result: "96개 검수" },
  { receivingNo: "RCV-20260518-001", time: "10:31", worker: "한지훈", device: "PDA-01", event: "로케이션 스캔", barcode: "A-01-03", result: "적치 72개" },
  { receivingNo: "RCV-20260518-002", time: "11:02", worker: "오세린", device: "PDA-03", event: "상품 검수", barcode: "SKU-8801", result: "80개 검수" },
  { receivingNo: "RCV-20260518-002", time: "11:08", worker: "오세린", device: "PDA-03", event: "로케이션 스캔", barcode: "B-02-01", result: "적치 완료" },
  { receivingNo: "RCV-20260518-003", time: "13:10", worker: "김도현", device: "PDA-02", event: "상품 검수", barcode: "SKU-1024", result: "파손 4개" },
  { receivingNo: "RCV-20260518-003", time: "13:24", worker: "김도현", device: "PDA-02", event: "로케이션 스캔", barcode: "C-04-05", result: "적치 120개" },
];

const inventoryRows: InventoryRow[] = [
  {
    sku: "SKU-4012",
    name: "Basic Tee / Black",
    client: "A 고객사",
    warehouse: "수도권 1센터",
    location: "A-01-03",
    lot: "LOT-260518-A",
    available: 324,
    allocated: 48,
    hold: 0,
    lastMovedAt: "2026-05-18",
    status: "정상",
  },
  {
    sku: "SKU-8801",
    name: "Daily Cap / Navy",
    client: "B 고객사",
    warehouse: "수도권 1센터",
    location: "B-02-01",
    lot: "LOT-260518-B",
    available: 91,
    allocated: 18,
    hold: 4,
    lastMovedAt: "2026-05-18",
    status: "보류",
  },
  {
    sku: "SKU-1024",
    name: "Slim Bottle / Clear",
    client: "A 고객사",
    warehouse: "부산 2센터",
    location: "C-04-05",
    lot: "LOT-260519-A",
    available: 612,
    allocated: 76,
    hold: 0,
    lastMovedAt: "2026-05-19",
    status: "정상",
  },
  {
    sku: "SKU-7780",
    name: "Pouch Set / Gray",
    client: "C 고객사",
    warehouse: "수도권 1센터",
    location: "D-01-02",
    lot: "LOT-260519-C",
    available: 38,
    allocated: 31,
    hold: 2,
    lastMovedAt: "2026-05-19",
    status: "부족주의",
  },
];

const locationCells: LocationCell[] = [
  { code: "A-01-01", zone: "A", bay: "01", level: "01", capacity: 120, used: 92, status: "정상", handlingType: "피킹" },
  { code: "A-01-02", zone: "A", bay: "01", level: "02", capacity: 120, used: 74, status: "정상", handlingType: "피킹" },
  { code: "A-01-03", zone: "A", bay: "01", level: "03", capacity: 140, used: 116, status: "정상", handlingType: "피킹" },
  { code: "A-02-01", zone: "A", bay: "02", level: "01", capacity: 100, used: 34, status: "빈 로케이션", handlingType: "보관" },
  { code: "B-01-01", zone: "B", bay: "01", level: "01", capacity: 110, used: 86, status: "정상", handlingType: "보관" },
  { code: "B-02-01", zone: "B", bay: "02", level: "01", capacity: 100, used: 94, status: "보류", handlingType: "피킹" },
  { code: "B-02-02", zone: "B", bay: "02", level: "02", capacity: 100, used: 68, status: "정상", handlingType: "보관" },
  { code: "C-03-01", zone: "C", bay: "03", level: "01", capacity: 160, used: 127, status: "정상", handlingType: "검수" },
  { code: "C-04-05", zone: "C", bay: "04", level: "05", capacity: 180, used: 171, status: "주의", handlingType: "보관" },
  { code: "D-01-01", zone: "D", bay: "01", level: "01", capacity: 90, used: 49, status: "정상", handlingType: "반품" },
  { code: "D-01-02", zone: "D", bay: "01", level: "02", capacity: 90, used: 83, status: "주의", handlingType: "피킹" },
  { code: "D-02-01", zone: "D", bay: "02", level: "01", capacity: 120, used: 0, status: "빈 로케이션", handlingType: "보관" },
];

const outboundRows: OutboundRow[] = [
  {
    no: "OUT-20260518-0801",
    client: "A 고객사",
    warehouse: "수도권 1센터",
    channel: "고객사 API",
    recipient: "김서연",
    requestedAt: "2026-05-18",
    lines: 12,
    allocated: 12,
    picked: 8,
    status: "할당완료",
  },
  {
    no: "OUT-20260518-0802",
    client: "B 고객사",
    warehouse: "수도권 1센터",
    channel: "CSV 업로드",
    recipient: "박민준",
    requestedAt: "2026-05-18",
    lines: 7,
    allocated: 5,
    picked: 0,
    status: "지시접수",
  },
  {
    no: "OUT-20260518-0803",
    client: "C 고객사",
    warehouse: "부산 2센터",
    channel: "관리자 등록",
    recipient: "이하은",
    requestedAt: "2026-05-19",
    lines: 18,
    allocated: 15,
    picked: 0,
    status: "재고부족",
  },
  {
    no: "OUT-20260519-0804",
    client: "A 고객사",
    warehouse: "수도권 1센터",
    channel: "고객사 API",
    recipient: "최도윤",
    requestedAt: "2026-05-19",
    lines: 9,
    allocated: 9,
    picked: 9,
    status: "피킹완료",
  },
];

const outboundInvoiceRows: InvoiceRow[] = [
  { outboundNo: "OUT-20260518-0801", invoiceNo: "INV-260518-0001", sku: "SKU-4012", product: "Basic Tee / Black", quantity: 2, allocated: 2, picked: 2, carrier: "CJ대한통운", printStatus: "출력완료" },
  { outboundNo: "OUT-20260518-0801", invoiceNo: "INV-260518-0002", sku: "SKU-1024", product: "Slim Bottle / Clear", quantity: 1, allocated: 1, picked: 1, carrier: "CJ대한통운", printStatus: "출력대기" },
  { outboundNo: "OUT-20260518-0801", invoiceNo: "INV-260518-0003", sku: "SKU-7780", product: "Pouch Set / Gray", quantity: 3, allocated: 3, picked: 0, carrier: "롯데택배", printStatus: "출력대기" },
  { outboundNo: "OUT-20260518-0802", invoiceNo: "INV-260518-0004", sku: "SKU-8801", product: "Daily Cap / Navy", quantity: 1, allocated: 1, picked: 0, carrier: "한진택배", printStatus: "미생성" },
  { outboundNo: "OUT-20260518-0802", invoiceNo: "INV-260518-0005", sku: "SKU-4012", product: "Basic Tee / Black", quantity: 4, allocated: 2, picked: 0, carrier: "한진택배", printStatus: "미생성" },
  { outboundNo: "OUT-20260518-0803", invoiceNo: "INV-260519-0006", sku: "SKU-7780", product: "Pouch Set / Gray", quantity: 6, allocated: 3, picked: 0, carrier: "CJ대한통운", printStatus: "보류" },
  { outboundNo: "OUT-20260519-0804", invoiceNo: "INV-260519-0007", sku: "SKU-1024", product: "Slim Bottle / Clear", quantity: 2, allocated: 2, picked: 2, carrier: "우체국택배", printStatus: "출력완료" },
];

const pickingRows: PickingRow[] = [
  {
    wave: "WAVE-0518-AM-01",
    client: "A 고객사",
    warehouse: "수도권 1센터",
    zone: "A",
    startedAt: "2026-05-18",
    orders: 18,
    tasks: 42,
    picked: 31,
    status: "진행중",
  },
  {
    wave: "WAVE-0518-AM-02",
    client: "B 고객사",
    warehouse: "수도권 1센터",
    zone: "B",
    startedAt: "2026-05-18",
    orders: 11,
    tasks: 26,
    picked: 26,
    status: "완료",
  },
  {
    wave: "WAVE-0518-PM-01",
    client: "C 고객사",
    warehouse: "부산 2센터",
    zone: "DPS",
    startedAt: "2026-05-19",
    orders: 24,
    tasks: 64,
    picked: 0,
    status: "대기",
  },
];

const waveInvoiceRows: WaveInvoiceRow[] = [
  { wave: "WAVE-0518-AM-01", invoiceNo: "INV-260518-0001", outboundNo: "OUT-20260518-0801", client: "A 고객사", recipient: "김서연", sku: "SKU-4012", product: "Basic Tee / Black", location: "A-01-03", quantity: 2, picked: 2, worker: "최유진", device: "PDA-05", status: "완료" },
  { wave: "WAVE-0518-AM-01", invoiceNo: "INV-260518-0002", outboundNo: "OUT-20260518-0801", client: "A 고객사", recipient: "김서연", sku: "SKU-1024", product: "Slim Bottle / Clear", location: "C-04-05", quantity: 1, picked: 1, worker: "최유진", device: "PDA-05", status: "완료" },
  { wave: "WAVE-0518-AM-01", invoiceNo: "INV-260518-0003", outboundNo: "OUT-20260518-0801", client: "A 고객사", recipient: "김서연", sku: "SKU-7780", product: "Pouch Set / Gray", location: "D-01-02", quantity: 3, picked: 0, worker: "문하늘", device: "PDA-06", status: "진행중" },
  { wave: "WAVE-0518-AM-02", invoiceNo: "INV-260518-0004", outboundNo: "OUT-20260518-0802", client: "B 고객사", recipient: "박민준", sku: "SKU-8801", product: "Daily Cap / Navy", location: "B-02-01", quantity: 1, picked: 1, worker: "오세린", device: "PDA-03", status: "완료" },
  { wave: "WAVE-0518-AM-02", invoiceNo: "INV-260518-0005", outboundNo: "OUT-20260518-0802", client: "B 고객사", recipient: "박민준", sku: "SKU-4012", product: "Basic Tee / Black", location: "A-01-03", quantity: 4, picked: 4, worker: "오세린", device: "PDA-03", status: "완료" },
  { wave: "WAVE-0518-PM-01", invoiceNo: "INV-260519-0006", outboundNo: "OUT-20260518-0803", client: "C 고객사", recipient: "이하은", sku: "SKU-7780", product: "Pouch Set / Gray", location: "D-01-02", quantity: 6, picked: 0, worker: "DPS", device: "DPS-AGENT", status: "대기" },
  { wave: "WAVE-0518-PM-01", invoiceNo: "INV-260519-0007", outboundNo: "OUT-20260519-0804", client: "A 고객사", recipient: "최도윤", sku: "SKU-1024", product: "Slim Bottle / Clear", location: "C-04-05", quantity: 2, picked: 0, worker: "DPS", device: "DPS-AGENT", status: "대기" },
];

const systemServiceRows = [
  { name: "API Server", endpoint: "http://localhost:8080", status: "설계 완료", note: "출고/입고/피킹 API" },
  { name: "PDF Renderer", endpoint: "http://localhost:4050", status: "예정", note: "송장/Picking List 렌더링" },
];

const localAgentRows = [
  { name: "DPS Protocol Agent", endpoint: "ws://localhost:4030/ws/dps", status: "연결 가능", note: "피킹 배치 시뮬레이터" },
  { name: "Print Agent", endpoint: "http://localhost:4040", status: "예정", note: "송장 출력 큐" },
  { name: "PDA Sync Client", endpoint: "Android local storage", status: "예정", note: "오프라인 작업 재전송 큐" },
];

export function App() {
  const [section, setSection] = useState<AdminSection>("dashboard");

  const title = useMemo(() => navItems.find((item) => item.id === section)?.label ?? "운영 현황", [section]);

  return (
    <div className="min-h-screen bg-[#eef2f3] text-[#1f2933]">
      <div className="grid min-h-screen grid-cols-[240px_1fr] max-lg:grid-cols-1">
        <aside className="border-r border-[#d7dee2] bg-[#f8faf9] px-4 py-5 max-lg:border-b max-lg:border-r-0">
          <div className="flex items-center gap-3 px-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-md bg-[#1b5e57] text-white">
              <Warehouse size={20} />
            </div>
            <div>
              <p className="text-sm font-semibold">Warehouse Ops</p>
              <p className="text-xs text-[#6b7780]">Admin Console</p>
            </div>
          </div>
          <nav className="mt-7 grid gap-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = item.id === section;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setSection(item.id)}
                  className={`flex h-10 items-center gap-3 rounded-md px-3 text-left text-sm transition ${
                    active
                      ? "bg-[#1b5e57] font-medium text-white"
                      : "text-[#4b5963] hover:bg-[#e6eceb] hover:text-[#1f2933]"
                  }`}
                >
                  <Icon size={17} />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </aside>

        <main className="min-w-0">
          <header className="flex min-h-16 items-center justify-between border-b border-[#d7dee2] bg-white px-6 max-sm:flex-col max-sm:items-start max-sm:gap-3 max-sm:py-4">
            <div>
              <h1 className="text-xl font-semibold tracking-normal">{title}</h1>
              <p className="mt-1 text-sm text-[#6b7780]">3PL WMS 운영 흐름 통합 관리</p>
            </div>
            <div className="flex items-center gap-2 text-sm text-[#4b5963]">
              <span className="h-2 w-2 rounded-full bg-[#2f9e44]" />
              Local demo
            </div>
          </header>

          <div className="grid gap-5 p-6 max-sm:p-4">
            {section === "dashboard" && <Dashboard />}
            {section === "receiving" && <ReceivingView />}
            {section === "inventory" && <InventoryView />}
            {section === "outbound" && <OutboundView />}
            {section === "picking" && <PickingView />}
            {section === "dps" && <DpsMonitor />}
            {section === "agents" && <AgentsView />}
          </div>
        </main>
      </div>
    </div>
  );
}

function Dashboard() {
  return (
    <div className="grid gap-5">
      <div className="grid grid-cols-4 gap-4 max-xl:grid-cols-2 max-sm:grid-cols-1">
        <Metric label="입고 진행" value={receivingRows.length.toLocaleString()} sub={`검수/적치 대상 ${sum(receivingRows, "requested").toLocaleString()}개`} icon={PackageCheck} tone="green" />
        <Metric label="가용 재고" value={sum(inventoryRows, "available").toLocaleString()} sub={`할당 ${sum(inventoryRows, "allocated").toLocaleString()} / 보류 ${sum(inventoryRows, "hold").toLocaleString()}`} icon={Boxes} tone="blue" />
        <Metric label="출고 지시" value={outboundRows.length.toLocaleString()} sub={`${outboundRows.filter((row) => row.status === "재고부족").length}건 재고 확인 필요`} icon={Truck} tone="amber" />
        <Metric label="피킹 작업" value={sum(pickingRows, "tasks").toLocaleString()} sub={`완료 ${sum(pickingRows, "picked").toLocaleString()} / 전체 ${sum(pickingRows, "tasks").toLocaleString()}`} icon={ClipboardList} tone="slate" />
      </div>

      <div className="grid grid-cols-[1.25fr_0.75fr] gap-5 max-xl:grid-cols-1">
        <SectionPanel title="운영 이슈 큐" action="조회">
          <div className="grid gap-2">
            {[
              ["입고 검수 대기", `${receivingRows.filter((row) => row.status === "검수중").length}건`, "검수 수량 반영 후 적치 작업 생성"],
              ["출고 할당 실패", `${outboundRows.filter((row) => row.status === "재고부족").length}건`, "가용 재고와 보류 재고 확인"],
              ["DPS 웨이브 대기", `${pickingRows.filter((row) => row.zone === "DPS" && row.status === "대기").length}건`, "DPS Agent 연결 후 작업 시작"],
              ["보류 재고", `${inventoryRows.filter((row) => row.hold > 0).length}건`, "파손/검수 이슈 처리 필요"],
            ].map(([label, count, desc]) => (
              <div key={label} className="grid grid-cols-[1fr_auto] items-center border-b border-[#e0e6e8] py-3 last:border-b-0">
                <div>
                  <p className="text-sm font-medium">{label}</p>
                  <p className="mt-1 text-xs text-[#6b7780]">{desc}</p>
                </div>
                <strong className="text-sm">{count}</strong>
              </div>
            ))}
          </div>
        </SectionPanel>

        <SectionPanel title="고객사별 처리 현황" action="SLA">
          <div className="grid gap-3">
            {clientOptions.slice(1).map((client) => {
              const clientOutbound = outboundRows.filter((row) => row.client === client);
              const completed = clientOutbound.filter((row) => row.status === "피킹완료").length;
              const rate = clientOutbound.length ? Math.round((completed / clientOutbound.length) * 100) : 0;
              return (
                <ProgressRow key={client} label={client} value={`${rate}%`} progress={rate} />
              );
            })}
          </div>
        </SectionPanel>
      </div>

      <div className="grid grid-cols-3 gap-5 max-xl:grid-cols-1">
        <MiniList
          title="최근 입고 내역"
          rows={receivingRows.slice(0, 4).map((row) => ({
            label: row.no,
            description: `${row.client} / ${row.warehouse} / ${row.worker}`,
            status: row.status,
          }))}
        />
        <MiniList
          title="최근 출고 내역"
          rows={outboundRows.slice(0, 4).map((row) => ({
            label: row.no,
            description: `${row.client} / ${row.recipient} / ${row.channel}`,
            status: row.status,
          }))}
        />
        <MiniList
          title="재고 주의"
          rows={inventoryRows.filter((row) => row.status !== "정상").map((row) => ({
            label: row.sku,
            description: `${row.client} / ${row.location} / 보류 ${row.hold.toLocaleString()}`,
            status: row.status,
          }))}
        />
      </div>
    </div>
  );
}

function ReceivingView() {
  const [filters, setFilters] = useState<OperationFilters>({
    ...defaultFilters,
    warehouse: "전체",
    status: "전체",
  });
  const [selectedNo, setSelectedNo] = useState(receivingRows[0]?.no ?? "");
  const rows = filterByOperation(receivingRows, filters, (row) => row.expectedDate, [
    "no",
    "client",
    "warehouse",
    "sku",
    "product",
    "supplier",
    "status",
    "worker",
    "device",
  ]);
  const selectedReceiving = rows.find((row) => row.no === selectedNo) ?? rows[0];
  const selectedSkuDetails = selectedReceiving
    ? receivingSkuDetails.filter((detail) => detail.receivingNo === selectedReceiving.no)
    : [];
  const selectedScanEvents = selectedReceiving
    ? receivingScanEvents.filter((event) => event.receivingNo === selectedReceiving.no)
    : [];

  return (
    <div className="grid gap-5">
      <ResultToolbar count={rows.length} label="PDA 입고 작업" actions={["입고 지시 등록", "PDA 현황", "엑셀"]} />
      <CompactFilterBar
        filters={filters}
        onChange={setFilters}
        statusOptions={["전체", "입고예정", "검수중", "적치중", "적치완료"]}
        keywordPlaceholder="입고번호, SKU, 상품명, 공급처"
        fields={["client", "warehouse", "status", "date", "keyword"]}
      />
      <div className="grid grid-cols-4 gap-4 max-xl:grid-cols-2 max-sm:grid-cols-1">
        <Metric label="입고 예정" value={sum(rows, "requested").toLocaleString()} sub="PDA 검수 대상" icon={PackageCheck} tone="green" />
        <Metric label="검수 완료" value={sum(rows, "received").toLocaleString()} sub="상품 바코드 스캔 기준" icon={ClipboardList} tone="blue" />
        <Metric label="적치 완료" value={sum(rows, "putaway").toLocaleString()} sub="로케이션 스캔 완료" icon={Warehouse} tone="slate" />
        <Metric label="미적치" value={(sum(rows, "received") - sum(rows, "putaway")).toLocaleString()} sub="현장 작업 대기" icon={Filter} tone="amber" />
      </div>
      <SectionPanel title="PDA 입고/적치 진행 현황" action="작업 상세">
        <ReceivingWorkTable rows={rows} selectedNo={selectedReceiving?.no ?? ""} onSelect={setSelectedNo} />
      </SectionPanel>
      {selectedReceiving && (
        <ReceivingWorkDetail
          receiving={selectedReceiving}
          skuDetails={selectedSkuDetails}
          scanEvents={selectedScanEvents}
        />
      )}
    </div>
  );
}

function InventoryView() {
  const [filters, setFilters] = useState<OperationFilters>({
    ...defaultFilters,
    status: "전체",
    fromDate: "",
    toDate: "",
  });
  const [selectedLocationCode, setSelectedLocationCode] = useState("A-01-03");
  const rows = filterByOperation(inventoryRows, filters, (row) => row.lastMovedAt, [
    "sku",
    "name",
    "client",
    "warehouse",
    "location",
    "lot",
    "status",
  ]);
  const selectedLocation =
    locationCells.find((location) => location.code === selectedLocationCode) ?? locationCells[0];
  const selectedLocationInventory = rows.filter((row) => row.location === selectedLocation.code);

  return (
    <div className="grid gap-5">
      <ResultToolbar count={rows.length} label="재고 레코드" actions={["재고 이동", "보류 전환", "CSV"]} />
      <CompactFilterBar
        filters={filters}
        onChange={setFilters}
        statusOptions={["전체", "정상", "보류", "부족주의"]}
        keywordPlaceholder="SKU, 상품명, 로케이션, LOT"
        fields={["client", "warehouse", "status", "keyword"]}
      />
      <div className="grid grid-cols-3 gap-4 max-lg:grid-cols-1">
        <Metric label="가용 수량" value={sum(rows, "available").toLocaleString()} sub="출고 할당 가능" icon={Boxes} tone="blue" />
        <Metric label="할당 수량" value={sum(rows, "allocated").toLocaleString()} sub="피킹 대기 포함" icon={ClipboardList} tone="slate" />
        <Metric label="보류 수량" value={sum(rows, "hold").toLocaleString()} sub="검수/파손/분실 이슈" icon={Filter} tone="amber" />
      </div>
      <LocationOverview
        locations={locationCells}
        selectedLocation={selectedLocation}
        selectedInventory={selectedLocationInventory}
        onSelect={setSelectedLocationCode}
      />
      <SectionPanel title="로케이션별 재고" action="상세 조회">
        <DataTable
          columns={["SKU", "고객사", "창고", "로케이션", "LOT", "가용", "할당", "보류", "최근 이동", "상태"]}
          rows={rows.map((row) => [
            <SkuCell key={row.sku} sku={row.sku} name={row.name} />,
            row.client,
            row.warehouse,
            row.location,
            row.lot,
            row.available.toLocaleString(),
            row.allocated.toLocaleString(),
            row.hold.toLocaleString(),
            row.lastMovedAt,
            <StatusPill key={`${row.sku}-status`} value={row.status} />,
          ])}
        />
      </SectionPanel>
    </div>
  );
}

function OutboundView() {
  const [filters, setFilters] = useState<OperationFilters>(defaultFilters);
  const rows = filterByOperation(outboundRows, filters, (row) => row.requestedAt, [
    "no",
    "client",
    "warehouse",
    "channel",
    "recipient",
    "status",
  ]);
  const [selectedNo, setSelectedNo] = useState(rows[0]?.no ?? "");
  const selectedOrder = rows.find((row) => row.no === selectedNo) ?? rows[0];
  const invoiceRows = selectedOrder ? outboundInvoiceRows.filter((invoice) => invoice.outboundNo === selectedOrder.no) : [];

  return (
    <div className="grid gap-5">
      <ResultToolbar count={rows.length} label="출고 지시" actions={["지시 등록", "일괄 할당", "엑셀"]} />
      <CompactFilterBar
        filters={filters}
        onChange={setFilters}
        statusOptions={["전체", "지시접수", "할당완료", "재고부족", "피킹완료"]}
        keywordPlaceholder="출고번호, 수취인, 채널"
        fields={["client", "status", "date", "keyword"]}
      />
      <SectionPanel title="출고 지시 접수 및 처리 내역" action="필터 저장">
        <OutboundOrderTable rows={rows} selectedNo={selectedOrder?.no ?? ""} onSelect={setSelectedNo} />
      </SectionPanel>
      {selectedOrder && (
        <SectionPanel title={`${selectedOrder.no} 송장 상세`} action={`${invoiceRows.length}건`}>
          <div className="mb-4 grid grid-cols-4 gap-3 max-lg:grid-cols-2 max-sm:grid-cols-1">
            <SummaryBox label="고객사" value={selectedOrder.client} />
            <SummaryBox label="수취인" value={selectedOrder.recipient} />
            <SummaryBox label="수집 경로" value={selectedOrder.channel} />
            <SummaryBox label="진행 상태" value={selectedOrder.status} />
          </div>
          <DataTable
            columns={["송장번호", "상품", "수량", "할당", "피킹", "택배사", "출력상태"]}
            rows={invoiceRows.map((invoice) => [
              invoice.invoiceNo,
              <SkuCell key={invoice.invoiceNo} sku={invoice.sku} name={invoice.product} />,
              invoice.quantity.toLocaleString(),
              invoice.allocated.toLocaleString(),
              invoice.picked.toLocaleString(),
              invoice.carrier,
              <StatusPill key={`${invoice.invoiceNo}-status`} value={invoice.printStatus} />,
            ])}
          />
        </SectionPanel>
      )}
    </div>
  );
}

function PickingView() {
  const [filters, setFilters] = useState<OperationFilters>({
    ...defaultFilters,
    warehouse: "전체",
  });
  const [selectedWave, setSelectedWave] = useState(pickingRows[0]?.wave ?? "");
  const rows = filterByOperation(pickingRows, filters, (row) => row.startedAt, [
    "wave",
    "client",
    "warehouse",
    "zone",
    "status",
  ]);
  const selectedPickingWave = rows.find((row) => row.wave === selectedWave) ?? rows[0];
  const selectedWaveInvoices = selectedPickingWave
    ? waveInvoiceRows.filter((invoice) => invoice.wave === selectedPickingWave.wave)
    : [];

  return (
    <div className="grid gap-5">
      <ResultToolbar count={rows.length} label="피킹 웨이브" actions={["웨이브 생성", "DPS 전송", "작업 배정"]} />
      <CompactFilterBar
        filters={filters}
        onChange={setFilters}
        statusOptions={["전체", "대기", "진행중", "완료"]}
        keywordPlaceholder="웨이브, 존, 고객사"
        fields={["client", "warehouse", "status", "date", "keyword"]}
      />
      <SectionPanel title="출고 웨이브 및 피킹 작업" action="작업자 배정">
        <PickingWaveTable rows={rows} selectedWave={selectedPickingWave?.wave ?? ""} onSelect={setSelectedWave} />
      </SectionPanel>
      {selectedPickingWave && (
        <SectionPanel title={`${selectedPickingWave.wave} 포함 송장`} action={`${selectedWaveInvoices.length}건`}>
          <div className="mb-4 grid grid-cols-4 gap-3 max-lg:grid-cols-2 max-sm:grid-cols-1">
            <SummaryBox label="고객사" value={selectedPickingWave.client} />
            <SummaryBox label="작업 존" value={selectedPickingWave.zone} />
            <SummaryBox label="주문/작업" value={`${selectedPickingWave.orders} / ${selectedPickingWave.tasks}`} />
            <SummaryBox label="진행 상태" value={selectedPickingWave.status} />
          </div>
          <DataTable
            columns={["송장번호", "출고번호", "고객사", "수취인", "상품", "로케이션", "지시", "피킹", "작업자", "상태"]}
            rows={selectedWaveInvoices.map((invoice) => [
              invoice.invoiceNo,
              invoice.outboundNo,
              invoice.client,
              invoice.recipient,
              <SkuCell key={invoice.invoiceNo} sku={invoice.sku} name={invoice.product} />,
              invoice.location,
              invoice.quantity.toLocaleString(),
              invoice.picked.toLocaleString(),
              `${invoice.worker} / ${invoice.device}`,
              <StatusPill key={`${invoice.invoiceNo}-status`} value={invoice.status} />,
            ])}
          />
        </SectionPanel>
      )}
    </div>
  );
}

function DpsMonitor() {
  const [connected, setConnected] = useState(false);
  const [events, setEvents] = useState<DpsEvent[]>([]);
  const [snapshot, setSnapshot] = useState<DpsSimulatorSnapshot | null>(null);
  const socketRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    const socket = new WebSocket("ws://localhost:4030/ws/dps");
    socketRef.current = socket;
    socket.onopen = () => setConnected(true);
    socket.onclose = () => setConnected(false);
    socket.onerror = () => setConnected(false);
    socket.onmessage = (event) => {
      const parsed = JSON.parse(event.data) as DpsEvent;
      setEvents((previous) => [parsed, ...previous].slice(0, 8));
      void refreshSnapshot();
    };
    return () => socket.close();
  }, []);

  async function refreshSnapshot() {
    try {
      const response = await fetch("/dps-agent/simulator/state");
      if (response.ok) {
        setSnapshot((await response.json()) as DpsSimulatorSnapshot);
      }
    } catch {
      setSnapshot(null);
    }
  }

  function startDemoBatch() {
    socketRef.current?.send(
      JSON.stringify({
        type: "PICKING_BATCH_STARTED",
        requestId: crypto.randomUUID(),
        payload: {
          batchId: "WAVE-0518-PM-01",
          cells: [
            { cellCode: "A-01", skuCode: "SKU-4012", quantity: 2 },
            { cellCode: "A-02", skuCode: "SKU-8801", quantity: 1 },
            { cellCode: "A-03", skuCode: "SKU-1024", quantity: 4 },
          ],
        },
      })
    );
  }

  async function confirmCell(cellCode: string, quantity: number) {
    await fetch(`/dps-agent/simulator/cells/${cellCode}/confirm`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ pickedQuantity: quantity }),
    });
    await refreshSnapshot();
  }

  return (
    <div className="grid gap-5">
      <div className="grid grid-cols-[1fr_auto] items-center gap-4 rounded-md border border-[#d7dee2] bg-white p-4 max-sm:grid-cols-1">
        <div>
          <div className="flex items-center gap-2">
            <StatusDot active={connected} />
            <h2 className="text-base font-semibold">DPS Protocol Agent</h2>
          </div>
          <p className="mt-1 text-sm text-[#6b7780]">ws://localhost:4030/ws/dps</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <IconButton label="새로고침" onClick={refreshSnapshot} icon={RefreshCcw} />
          <IconButton label="배치 시작" onClick={startDemoBatch} icon={Send} primary />
        </div>
      </div>

      <div className="grid grid-cols-[1.2fr_0.8fr] gap-5 max-xl:grid-cols-1">
        <SectionPanel title="DPS 셀 상태" action={snapshot?.status.activeBatchId ?? "대기"}>
          <div className="grid grid-cols-3 gap-3 max-md:grid-cols-1">
            {(snapshot?.cells ?? []).map((cell) => (
              <div key={cell.cellCode} className="rounded-md border border-[#d7dee2] bg-[#fbfcfb] p-4">
                <div className="flex items-center justify-between gap-2">
                  <strong className="text-lg">{cell.cellCode}</strong>
                  <StatusPill value={cell.state} />
                </div>
                <p className="mt-3 text-sm text-[#4b5963]">{cell.skuCode}</p>
                <p className="mt-1 text-sm text-[#6b7780]">지시 수량 {cell.quantity}</p>
                <button
                  type="button"
                  onClick={() => void confirmCell(cell.cellCode, cell.quantity)}
                  className="mt-4 flex h-9 w-full items-center justify-center gap-2 rounded-md bg-[#1b5e57] px-3 text-sm font-medium text-white hover:bg-[#174f49]"
                >
                  <CheckCircle2 size={16} />
                  완료
                </button>
              </div>
            ))}
            {!snapshot?.cells.length && (
              <div className="col-span-full rounded-md border border-dashed border-[#cbd5d9] bg-[#fbfcfb] p-8 text-center text-sm text-[#6b7780]">
                배치 시작 후 셀 상태가 표시됩니다.
              </div>
            )}
          </div>
        </SectionPanel>

        <SectionPanel title="최근 이벤트" action={`${events.length}건`}>
          <div className="grid gap-2">
            {events.map((event, index) => (
              <div key={`${event.type}-${index}`} className="border-b border-[#e0e6e8] py-3 last:border-b-0">
                <p className="text-sm font-medium">{event.type}</p>
                <p className="mt-1 line-clamp-2 text-xs text-[#6b7780]">{JSON.stringify(event.payload ?? {})}</p>
              </div>
            ))}
            {!events.length && <p className="py-8 text-center text-sm text-[#6b7780]">이벤트 대기중</p>}
          </div>
        </SectionPanel>
      </div>
    </div>
  );
}

function AgentsView() {
  return (
    <div className="grid gap-5">
      <SectionPanel title="현장 로컬 에이전트" action={`${localAgentRows.length}개`}>
        <DataTable
          columns={["프로그램", "로컬 엔드포인트", "상태", "역할"]}
          rows={localAgentRows.map((row) => [
            row.name,
            row.endpoint,
            <StatusPill key={row.name} value={row.status} />,
            row.note,
          ])}
        />
      </SectionPanel>
      <SectionPanel title="중앙 서비스 연결" action={`${systemServiceRows.length}개`}>
        <DataTable
          columns={["서비스", "엔드포인트", "상태", "역할"]}
          rows={systemServiceRows.map((row) => [
            row.name,
            row.endpoint,
            <StatusPill key={row.name} value={row.status} />,
            row.note,
          ])}
        />
      </SectionPanel>
    </div>
  );
}

function LocationOverview({
  locations,
  selectedLocation,
  selectedInventory,
  onSelect,
}: {
  locations: LocationCell[];
  selectedLocation: LocationCell;
  selectedInventory: InventoryRow[];
  onSelect: (code: string) => void;
}) {
  const zones = Array.from(new Set(locations.map((location) => location.zone)));
  const totalCapacity = sum(locations, "capacity");
  const totalUsed = sum(locations, "used");
  const usageRate = totalCapacity ? Math.round((totalUsed / totalCapacity) * 100) : 0;

  return (
    <section className="rounded-md border border-[#d7dee2] bg-white">
      <div className="flex min-h-12 items-center justify-between border-b border-[#e0e6e8] px-4">
        <div>
          <h2 className="text-sm font-semibold">로케이션 맵</h2>
          <p className="mt-0.5 text-xs text-[#6b7780]">존, 베이, 레벨 기준 보관 상태</p>
        </div>
        <div className="flex items-center gap-3 text-xs text-[#6b7780] max-sm:hidden">
          <LegendDot className="bg-[#1b5e57]" label="정상" />
          <LegendDot className="bg-[#c98a14]" label="주의" />
          <LegendDot className="bg-[#9b2c2c]" label="보류" />
          <LegendDot className="bg-[#cbd5d9]" label="빈 로케이션" />
        </div>
      </div>
      <div className="grid grid-cols-[1.25fr_0.75fr] gap-5 p-4 max-xl:grid-cols-1">
        <div className="grid gap-4">
          <div className="grid grid-cols-3 gap-3 max-md:grid-cols-1">
            <SummaryBox label="전체 로케이션" value={`${locations.length.toLocaleString()}개`} />
            <SummaryBox label="평균 점유율" value={`${usageRate}%`} />
            <SummaryBox label="주의/보류" value={`${locations.filter((location) => location.status === "주의" || location.status === "보류").length}개`} />
          </div>
          <div className="grid grid-cols-4 gap-3 max-2xl:grid-cols-2 max-sm:grid-cols-1">
            {zones.map((zone) => {
              const zoneLocations = locations.filter((location) => location.zone === zone);
              return (
                <div key={zone} className="rounded-md border border-[#d7dee2] bg-[#fbfcfb] p-3">
                  <div className="mb-3 flex items-center justify-between">
                    <strong className="text-sm">Zone {zone}</strong>
                    <span className="text-xs text-[#6b7780]">{zoneLocations.length} cells</span>
                  </div>
                  <div className="grid grid-cols-3 gap-2">
                    {zoneLocations.map((location) => {
                      const selected = location.code === selectedLocation.code;
                      return (
                        <button
                          key={location.code}
                          type="button"
                          title={`${location.code} ${location.status}`}
                          onClick={() => onSelect(location.code)}
                          className={`aspect-square rounded-md border p-2 text-left transition ${
                            selected
                              ? "border-[#1b5e57] bg-[#e6f0ee] ring-2 ring-[#1b5e57]/20"
                              : "border-[#d7dee2] bg-white hover:border-[#9fb5b1]"
                          }`}
                        >
                          <span className={`mb-2 block h-2 rounded-full ${locationStatusColor(location.status)}`} />
                          <span className="block text-xs font-semibold">{location.code}</span>
                          <span className="mt-1 block text-[11px] text-[#6b7780]">{locationUsage(location)}%</span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div className="rounded-md border border-[#d7dee2] bg-[#fbfcfb] p-4">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-xs text-[#6b7780]">선택 로케이션</p>
              <h3 className="mt-1 text-xl font-semibold">{selectedLocation.code}</h3>
            </div>
            <StatusPill value={selectedLocation.status} />
          </div>
          <div className="mt-4 grid grid-cols-2 gap-3">
            <SummaryBox label="존/베이/레벨" value={`${selectedLocation.zone}-${selectedLocation.bay}-${selectedLocation.level}`} />
            <SummaryBox label="작업 유형" value={selectedLocation.handlingType} />
            <SummaryBox label="적재 수량" value={`${selectedLocation.used.toLocaleString()} / ${selectedLocation.capacity.toLocaleString()}`} />
            <SummaryBox label="점유율" value={`${locationUsage(selectedLocation)}%`} />
          </div>
          <div className="mt-4">
            <ProgressBar value={locationUsage(selectedLocation)} />
          </div>
          <div className="mt-5">
            <p className="mb-2 text-sm font-semibold">보관 SKU</p>
            <div className="grid gap-2">
              {selectedInventory.map((row) => (
                <div key={`${row.location}-${row.sku}`} className="rounded-md border border-[#d7dee2] bg-white p-3">
                  <SkuCell sku={row.sku} name={row.name} />
                  <p className="mt-2 text-xs text-[#6b7780]">
                    가용 {row.available.toLocaleString()} / 할당 {row.allocated.toLocaleString()} / 보류 {row.hold.toLocaleString()}
                  </p>
                </div>
              ))}
              {!selectedInventory.length && (
                <div className="rounded-md border border-dashed border-[#cbd5d9] bg-white p-5 text-center text-sm text-[#6b7780]">
                  현재 보관 중인 SKU가 없습니다.
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function LegendDot({ className, label }: { className: string; label: string }) {
  return (
    <span className="inline-flex items-center gap-1">
      <span className={`h-2 w-2 rounded-full ${className}`} />
      {label}
    </span>
  );
}

function locationUsage(location: LocationCell): number {
  return location.capacity ? Math.round((location.used / location.capacity) * 100) : 0;
}

function locationStatusColor(status: LocationCell["status"]): string {
  if (status === "보류") {
    return "bg-[#9b2c2c]";
  }
  if (status === "주의") {
    return "bg-[#c98a14]";
  }
  if (status === "빈 로케이션") {
    return "bg-[#cbd5d9]";
  }
  return "bg-[#1b5e57]";
}

function CompactFilterBar({
  filters,
  onChange,
  statusOptions,
  keywordPlaceholder,
  fields,
}: {
  filters: OperationFilters;
  onChange: (filters: OperationFilters) => void;
  statusOptions: string[];
  keywordPlaceholder: string;
  fields: Array<"client" | "warehouse" | "status" | "date" | "keyword">;
}) {
  const update = (patch: Partial<OperationFilters>) => onChange({ ...filters, ...patch });

  return (
    <section className="rounded-md border border-[#d7dee2] bg-white px-4 py-3">
      <div className="grid grid-cols-[auto_1fr_auto] items-center gap-3 max-md:grid-cols-1">
        <div className="flex h-10 items-center gap-2 text-sm font-semibold text-[#2f3a42]">
          <SlidersHorizontal size={17} />
          조회 조건
        </div>
        <div className="grid grid-cols-[repeat(auto-fit,minmax(150px,1fr))] gap-2">
          {fields.includes("client") && (
            <SelectField label="고객사" value={filters.client} options={clientOptions} onChange={(client) => update({ client })} compact />
          )}
          {fields.includes("warehouse") && (
            <SelectField label="창고" value={filters.warehouse} options={warehouseOptions} onChange={(warehouse) => update({ warehouse })} compact />
          )}
          {fields.includes("status") && (
            <SelectField label="상태" value={filters.status} options={statusOptions} onChange={(status) => update({ status })} compact />
          )}
          {fields.includes("date") && (
            <DateRangeFields
              fromDate={filters.fromDate}
              toDate={filters.toDate}
              onChange={(patch) => update(patch)}
            />
          )}
          {fields.includes("keyword") && (
            <TextField label="검색" value={filters.keyword} placeholder={keywordPlaceholder} onChange={(keyword) => update({ keyword })} compact />
          )}
        </div>
        <button
          type="button"
          onClick={() => onChange(defaultFilters)}
          className="flex h-9 items-center justify-center gap-1 rounded-md border border-[#cbd5d9] px-3 text-xs text-[#4b5963] hover:bg-[#f4f7f7]"
        >
          <X size={14} />
          초기화
        </button>
      </div>
    </section>
  );
}

function ResultToolbar({ count, label, actions }: { count: number; label: string; actions: string[] }) {
  return (
    <div className="grid grid-cols-[1fr_auto] items-center gap-3 rounded-md border border-[#d7dee2] bg-white p-4 max-xl:grid-cols-1">
      <div>
        <p className="text-sm text-[#6b7780]">조회 결과</p>
        <p className="mt-1 text-lg font-semibold">
          {label} {count.toLocaleString()}건
        </p>
      </div>
      <div className="flex flex-wrap justify-end gap-2 max-xl:justify-start">
        {actions.map((action, index) => (
          <button
            key={action}
            type="button"
            className={`flex h-9 items-center gap-2 rounded-md px-3 text-sm font-medium ${
              index === 0
                ? "bg-[#1b5e57] text-white hover:bg-[#174f49]"
                : "border border-[#cbd5d9] bg-white text-[#3f4a52] hover:bg-[#f4f7f7]"
            }`}
          >
            {action === "엑셀" || action === "CSV" ? <Download size={15} /> : <ChevronRight size={15} />}
            {action}
          </button>
        ))}
      </div>
    </div>
  );
}

function OutboundOrderTable({
  rows,
  selectedNo,
  onSelect,
}: {
  rows: OutboundRow[];
  selectedNo: string;
  onSelect: (no: string) => void;
}) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[920px] border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-[#d7dee2] text-xs uppercase text-[#6b7780]">
            {["출고번호", "고객사", "창고", "수집 경로", "수취인", "요청일", "라인", "할당", "피킹", "진행률", "상태"].map((column) => (
              <th key={column} className="px-3 py-3 font-medium">
                {column}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const selected = row.no === selectedNo;
            return (
              <tr
                key={row.no}
                onClick={() => onSelect(row.no)}
                className={`cursor-pointer border-b border-[#edf1f2] last:border-b-0 ${
                  selected ? "bg-[#e6f0ee]" : "hover:bg-[#f8faf9]"
                }`}
              >
                <td className="px-3 py-3 font-medium text-[#1b5e57]">{row.no}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.client}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.warehouse}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.channel}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.recipient}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.requestedAt}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.lines.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.allocated.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.picked.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">
                  <ProgressBar value={row.lines ? Math.round((row.picked / row.lines) * 100) : 0} />
                </td>
                <td className="px-3 py-3 text-[#2f3a42]">
                  <StatusPill value={row.status} />
                </td>
              </tr>
            );
          })}
          {!rows.length && (
            <tr>
              <td className="px-3 py-10 text-center text-sm text-[#6b7780]" colSpan={11}>
                조회 조건에 맞는 출고 지시가 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function PickingWaveTable({
  rows,
  selectedWave,
  onSelect,
}: {
  rows: PickingRow[];
  selectedWave: string;
  onSelect: (wave: string) => void;
}) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[920px] border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-[#d7dee2] text-xs uppercase text-[#6b7780]">
            {["웨이브", "고객사", "창고", "존", "시작일", "주문", "작업", "완료", "송장", "진행률", "상태"].map((column) => (
              <th key={column} className="px-3 py-3 font-medium">
                {column}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const selected = row.wave === selectedWave;
            const invoiceCount = waveInvoiceRows.filter((invoice) => invoice.wave === row.wave).length;
            return (
              <tr
                key={row.wave}
                onClick={() => onSelect(row.wave)}
                className={`cursor-pointer border-b border-[#edf1f2] last:border-b-0 ${
                  selected ? "bg-[#e6f0ee]" : "hover:bg-[#f8faf9]"
                }`}
              >
                <td className="px-3 py-3 font-medium text-[#1b5e57]">{row.wave}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.client}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.warehouse}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.zone}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.startedAt}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.orders.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.tasks.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.picked.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{invoiceCount.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">
                  <ProgressBar value={row.tasks ? Math.round((row.picked / row.tasks) * 100) : 0} />
                </td>
                <td className="px-3 py-3 text-[#2f3a42]">
                  <StatusPill value={row.status} />
                </td>
              </tr>
            );
          })}
          {!rows.length && (
            <tr>
              <td className="px-3 py-10 text-center text-sm text-[#6b7780]" colSpan={11}>
                조회 조건에 맞는 피킹 웨이브가 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function ReceivingWorkTable({
  rows,
  selectedNo,
  onSelect,
}: {
  rows: ReceivingRow[];
  selectedNo: string;
  onSelect: (no: string) => void;
}) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[980px] border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-[#d7dee2] text-xs uppercase text-[#6b7780]">
            {["입고번호", "고객사", "창고", "입고예정일", "작업자", "PDA", "예정", "검수", "적치", "미적치", "마지막 스캔", "상태"].map((column) => (
              <th key={column} className="px-3 py-3 font-medium">
                {column}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const selected = row.no === selectedNo;
            const notPutaway = Math.max(0, row.received - row.putaway);
            return (
              <tr
                key={row.no}
                onClick={() => onSelect(row.no)}
                className={`cursor-pointer border-b border-[#edf1f2] last:border-b-0 ${
                  selected ? "bg-[#e6f0ee]" : "hover:bg-[#f8faf9]"
                }`}
              >
                <td className="px-3 py-3 font-medium text-[#1b5e57]">{row.no}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.client}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.warehouse}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.expectedDate}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.worker}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.device}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.requested.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.received.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.putaway.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{notPutaway.toLocaleString()}</td>
                <td className="px-3 py-3 text-[#2f3a42]">{row.lastScannedAt}</td>
                <td className="px-3 py-3 text-[#2f3a42]">
                  <StatusPill value={row.status} />
                </td>
              </tr>
            );
          })}
          {!rows.length && (
            <tr>
              <td className="px-3 py-10 text-center text-sm text-[#6b7780]" colSpan={12}>
                조회 조건에 맞는 PDA 입고 작업이 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function ReceivingWorkDetail({
  receiving,
  skuDetails,
  scanEvents,
}: {
  receiving: ReceivingRow;
  skuDetails: ReceivingSkuDetail[];
  scanEvents: ReceivingScanEvent[];
}) {
  const inspected = sum(skuDetails, "inspected");
  const putaway = sum(skuDetails, "putaway");

  return (
    <div className="grid grid-cols-[1fr_0.75fr] gap-5 max-xl:grid-cols-1">
      <SectionPanel title={`${receiving.no} SKU 검수/적치 상세`} action={`${skuDetails.length}개 SKU`}>
        <div className="mb-4 grid grid-cols-4 gap-3 max-lg:grid-cols-2 max-sm:grid-cols-1">
          <SummaryBox label="담당 작업자" value={`${receiving.worker} / ${receiving.device}`} />
          <SummaryBox label="공급처" value={receiving.supplier} />
          <SummaryBox label="검수 진행" value={`${inspected.toLocaleString()} / ${receiving.requested.toLocaleString()}`} />
          <SummaryBox label="적치 진행" value={`${putaway.toLocaleString()} / ${inspected.toLocaleString()}`} />
        </div>
        <DataTable
          columns={["SKU", "예정", "검수", "적치", "파손", "부족", "적치 로케이션", "진행률"]}
          rows={skuDetails.map((detail) => [
            <SkuCell key={detail.sku} sku={detail.sku} name={detail.product} />,
            detail.requested.toLocaleString(),
            detail.inspected.toLocaleString(),
            detail.putaway.toLocaleString(),
            detail.damaged.toLocaleString(),
            detail.shortage.toLocaleString(),
            detail.targetLocations.join(", "),
            <ProgressBar key={`${detail.sku}-putaway`} value={detail.inspected ? Math.round((detail.putaway / detail.inspected) * 100) : 0} />,
          ])}
        />
      </SectionPanel>

      <SectionPanel title="PDA 스캔 이벤트" action={`${scanEvents.length}건`}>
        <div className="grid gap-2">
          {scanEvents.map((event) => (
            <div key={`${event.time}-${event.barcode}`} className="rounded-md border border-[#d7dee2] bg-[#fbfcfb] p-3">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold">{event.event}</p>
                  <p className="mt-1 text-xs text-[#6b7780]">
                    {event.worker} / {event.device}
                  </p>
                </div>
                <span className="text-xs font-medium text-[#4b5963]">{event.time}</span>
              </div>
              <div className="mt-3 grid grid-cols-[1fr_auto] items-center gap-3 text-xs">
                <span className="truncate rounded-md bg-white px-2 py-1 text-[#4b5963]">{event.barcode}</span>
                <StatusPill value={event.result} />
              </div>
            </div>
          ))}
          {!scanEvents.length && (
            <div className="rounded-md border border-dashed border-[#cbd5d9] p-6 text-center text-sm text-[#6b7780]">
              아직 PDA 스캔 이벤트가 없습니다.
            </div>
          )}
        </div>
      </SectionPanel>
    </div>
  );
}

function SummaryBox({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border border-[#d7dee2] bg-[#fbfcfb] p-3">
      <p className="text-xs text-[#6b7780]">{label}</p>
      <p className="mt-1 truncate text-sm font-semibold text-[#1f2933]">{value}</p>
    </div>
  );
}

function Metric({
  label,
  value,
  sub,
  icon: Icon,
  tone,
}: {
  label: string;
  value: string;
  sub: string;
  icon: typeof LayoutDashboard;
  tone: "green" | "blue" | "amber" | "slate";
}) {
  const tones = {
    green: "bg-[#dceee8] text-[#1b5e57]",
    blue: "bg-[#ddebf6] text-[#28527a]",
    amber: "bg-[#f4ead2] text-[#836016]",
    slate: "bg-[#e4e8eb] text-[#3f4a52]",
  };
  return (
    <div className="rounded-md border border-[#d7dee2] bg-white p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-[#6b7780]">{label}</p>
          <p className="mt-2 text-2xl font-semibold">{value}</p>
        </div>
        <div className={`flex h-9 w-9 items-center justify-center rounded-md ${tones[tone]}`}>
          <Icon size={18} />
        </div>
      </div>
      <p className="mt-3 text-xs text-[#6b7780]">{sub}</p>
    </div>
  );
}

function SectionPanel({ title, action, children }: { title: string; action: string; children: ReactNode }) {
  return (
    <section className="rounded-md border border-[#d7dee2] bg-white">
      <div className="flex min-h-12 items-center justify-between border-b border-[#e0e6e8] px-4">
        <h2 className="text-sm font-semibold">{title}</h2>
        <button type="button" className="flex items-center gap-1 text-sm font-medium text-[#1b5e57]">
          {action}
          <ChevronRight size={15} />
        </button>
      </div>
      <div className="p-4">{children}</div>
    </section>
  );
}

function DataTable({ columns, rows }: { columns: string[]; rows: Array<Array<ReactNode>> }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[920px] border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-[#d7dee2] text-xs uppercase text-[#6b7780]">
            {columns.map((column) => (
              <th key={column} className="px-3 py-3 font-medium">
                {column}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex} className="border-b border-[#edf1f2] last:border-b-0 hover:bg-[#f8faf9]">
              {row.map((cell, cellIndex) => (
                <td key={cellIndex} className="px-3 py-3 text-[#2f3a42]">
                  {cell}
                </td>
              ))}
            </tr>
          ))}
          {!rows.length && (
            <tr>
              <td className="px-3 py-10 text-center text-sm text-[#6b7780]" colSpan={columns.length}>
                조회 조건에 맞는 데이터가 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function SelectField({
  label,
  value,
  options,
  onChange,
  compact = false,
}: {
  label: string;
  value: string;
  options: string[];
  onChange: (value: string) => void;
  compact?: boolean;
}) {
  return (
    <label className="grid gap-1 text-xs font-medium text-[#6b7780]">
      <span className={compact ? "sr-only" : undefined}>{label}</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className={`${compact ? "h-9" : "h-10"} rounded-md border border-[#cbd5d9] bg-white px-3 text-sm text-[#1f2933] outline-none focus:border-[#1b5e57]`}
      >
        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </label>
  );
}

function DateRangeFields({
  fromDate,
  toDate,
  onChange,
}: {
  fromDate: string;
  toDate: string;
  onChange: (patch: Pick<OperationFilters, "fromDate"> | Pick<OperationFilters, "toDate">) => void;
}) {
  return (
    <div className="grid min-w-[260px] grid-cols-2 gap-2 max-sm:min-w-0">
      <DateInput label="시작일" value={fromDate} onChange={(value) => onChange({ fromDate: value })} />
      <DateInput label="종료일" value={toDate} onChange={(value) => onChange({ toDate: value })} />
    </div>
  );
}

function DateInput({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="grid gap-1 text-xs font-medium text-[#6b7780]">
      <span className="sr-only">{label}</span>
      <span className="relative">
        <CalendarDays className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[#6b7780]" size={15} />
        <input
          type="date"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          className="h-9 w-full rounded-md border border-[#cbd5d9] bg-white pl-9 pr-3 text-sm text-[#1f2933] outline-none focus:border-[#1b5e57]"
        />
      </span>
    </label>
  );
}

function TextField({
  label,
  value,
  placeholder,
  onChange,
  compact = false,
}: {
  label: string;
  value: string;
  placeholder: string;
  onChange: (value: string) => void;
  compact?: boolean;
}) {
  return (
    <label className="grid gap-1 text-xs font-medium text-[#6b7780]">
      <span className={compact ? "sr-only" : undefined}>{label}</span>
      <span className="relative">
        <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[#6b7780]" size={15} />
        <input
          type="search"
          value={value}
          placeholder={placeholder}
          onChange={(event) => onChange(event.target.value)}
          className={`${compact ? "h-9" : "h-10"} w-full rounded-md border border-[#cbd5d9] bg-white pl-9 pr-3 text-sm text-[#1f2933] outline-none focus:border-[#1b5e57]`}
        />
      </span>
    </label>
  );
}

function StatusPill({ value }: { value: string }) {
  const normalized = value.toLowerCase();
  const tone =
    normalized.includes("완료") || normalized.includes("completed") || normalized.includes("연결") || normalized.includes("정상")
      ? "bg-[#dceee8] text-[#1b5e57]"
      : normalized.includes("부족") || normalized.includes("failed") || normalized.includes("보류")
        ? "bg-[#f8dddd] text-[#9b2c2c]"
        : normalized.includes("light") || normalized.includes("진행") || normalized.includes("가능") || normalized.includes("검수")
          ? "bg-[#ddebf6] text-[#28527a]"
          : "bg-[#e7eaed] text-[#4b5963]";
  return <span className={`inline-flex h-7 items-center rounded-md px-2 text-xs font-medium ${tone}`}>{value}</span>;
}

function StatusDot({ active }: { active: boolean }) {
  return <span className={`h-2.5 w-2.5 rounded-full ${active ? "bg-[#2f9e44]" : "bg-[#c92a2a]"}`} />;
}

function IconButton({
  label,
  icon: Icon,
  onClick,
  primary = false,
}: {
  label: string;
  icon: typeof Activity;
  onClick: () => void;
  primary?: boolean;
}) {
  return (
    <button
      type="button"
      title={label}
      onClick={onClick}
      className={`flex h-9 items-center gap-2 rounded-md px-3 text-sm font-medium ${
        primary
          ? "bg-[#1b5e57] text-white hover:bg-[#174f49]"
          : "border border-[#cbd5d9] bg-white text-[#3f4a52] hover:bg-[#f4f7f7]"
      }`}
    >
      <Icon size={16} />
      {label}
    </button>
  );
}

function SkuCell({ sku, name }: { sku: string; name: string }) {
  return (
    <div>
      <p className="font-medium text-[#1f2933]">{sku}</p>
      <p className="mt-0.5 text-xs text-[#6b7780]">{name}</p>
    </div>
  );
}

function ProgressBar({ value }: { value: number }) {
  const normalized = Math.max(0, Math.min(100, value));
  return (
    <div className="min-w-28">
      <div className="h-2 rounded-full bg-[#e7ecef]">
        <div className="h-2 rounded-full bg-[#1b5e57]" style={{ width: `${normalized}%` }} />
      </div>
      <p className="mt-1 text-xs text-[#6b7780]">{normalized}%</p>
    </div>
  );
}

function ProgressRow({ label, value, progress }: { label: string; value: string; progress: number }) {
  return (
    <div>
      <div className="mb-2 flex items-center justify-between gap-3 text-sm">
        <span className="font-medium">{label}</span>
        <span className="text-[#6b7780]">{value}</span>
      </div>
      <ProgressBar value={progress} />
    </div>
  );
}

function MiniList({
  title,
  rows,
}: {
  title: string;
  rows: Array<{ label: string; description: string; status: string }>;
}) {
  return (
    <SectionPanel title={title} action={`${rows.length}건`}>
      <div className="grid gap-2">
        {rows.map((row) => (
          <div key={row.label} className="grid grid-cols-[1fr_auto] items-center gap-3 border-b border-[#edf1f2] py-2 last:border-b-0">
            <div className="min-w-0">
              <p className="truncate text-sm font-medium">{row.label}</p>
              <p className="mt-0.5 truncate text-xs text-[#6b7780]">{row.description}</p>
            </div>
            <StatusPill value={row.status} />
          </div>
        ))}
        {!rows.length && <p className="py-6 text-center text-sm text-[#6b7780]">표시할 항목이 없습니다.</p>}
      </div>
    </SectionPanel>
  );
}

function filterByOperation<T extends { client: string; warehouse: string; status: string }>(
  rows: T[],
  filters: OperationFilters,
  dateAccessor: (row: T) => string,
  keywordKeys: Array<keyof T>
): T[] {
  const keyword = filters.keyword.trim().toLowerCase();
  return rows.filter((row) => {
    const date = dateAccessor(row);
    const clientMatched = filters.client === "전체" || row.client === filters.client;
    const warehouseMatched = filters.warehouse === "전체" || row.warehouse === filters.warehouse;
    const statusMatched = filters.status === "전체" || row.status === filters.status;
    const dateMatched = (!filters.fromDate || date >= filters.fromDate) && (!filters.toDate || date <= filters.toDate);
    const keywordMatched =
      !keyword ||
      keywordKeys.some((key) => {
        const value = row[key];
        return String(value).toLowerCase().includes(keyword);
      });
    return clientMatched && warehouseMatched && statusMatched && dateMatched && keywordMatched;
  });
}

function sum<T>(rows: T[], key: keyof T): number {
  return rows.reduce((total, row) => {
    const value = row[key];
    return total + (typeof value === "number" ? value : 0);
  }, 0);
}
