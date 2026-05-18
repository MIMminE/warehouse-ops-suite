import {
  Activity,
  Boxes,
  CheckCircle2,
  ChevronRight,
  ClipboardList,
  LayoutDashboard,
  PackageCheck,
  PlugZap,
  RadioTower,
  RefreshCcw,
  Send,
  Truck,
  Warehouse,
} from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";

type AdminSection =
  | "dashboard"
  | "receiving"
  | "inventory"
  | "outbound"
  | "picking"
  | "dps"
  | "agents";

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

const navItems: Array<{ id: AdminSection; label: string; icon: typeof LayoutDashboard }> = [
  { id: "dashboard", label: "운영 현황", icon: LayoutDashboard },
  { id: "receiving", label: "입고/적치", icon: PackageCheck },
  { id: "inventory", label: "재고", icon: Boxes },
  { id: "outbound", label: "출고 지시", icon: Truck },
  { id: "picking", label: "피킹 웨이브", icon: ClipboardList },
  { id: "dps", label: "DPS 모니터", icon: RadioTower },
  { id: "agents", label: "로컬 에이전트", icon: PlugZap },
];

const receivingRows = [
  { no: "RCV-20260518-001", client: "A 고객사", sku: "SKU-4012", requested: 120, received: 96, putaway: 72, status: "검수중" },
  { no: "RCV-20260518-002", client: "B 고객사", sku: "SKU-8801", requested: 80, received: 80, putaway: 80, status: "적치완료" },
  { no: "RCV-20260518-003", client: "A 고객사", sku: "SKU-1024", requested: 240, received: 210, putaway: 120, status: "적치중" },
];

const inventoryRows = [
  { sku: "SKU-4012", name: "Basic Tee / Black", location: "A-01-03", available: 324, allocated: 48, hold: 0 },
  { sku: "SKU-8801", name: "Daily Cap / Navy", location: "B-02-01", available: 91, allocated: 18, hold: 4 },
  { sku: "SKU-1024", name: "Slim Bottle / Clear", location: "C-04-05", available: 612, allocated: 76, hold: 0 },
  { sku: "SKU-7780", name: "Pouch Set / Gray", location: "D-01-02", available: 38, allocated: 31, hold: 2 },
];

const outboundRows = [
  { no: "OUT-20260518-0801", client: "A 고객사", lines: 12, requestedAt: "2026-05-18", status: "할당완료" },
  { no: "OUT-20260518-0802", client: "B 고객사", lines: 7, requestedAt: "2026-05-18", status: "지시접수" },
  { no: "OUT-20260518-0803", client: "C 고객사", lines: 18, requestedAt: "2026-05-19", status: "재고부족" },
];

const pickingRows = [
  { wave: "WAVE-0518-AM-01", zone: "A", orders: 18, tasks: 42, picked: 31, status: "진행중" },
  { wave: "WAVE-0518-AM-02", zone: "B", orders: 11, tasks: 26, picked: 26, status: "완료" },
  { wave: "WAVE-0518-PM-01", zone: "DPS", orders: 24, tasks: 64, picked: 0, status: "대기" },
];

const agentRows = [
  { name: "API Server", endpoint: "http://localhost:8080", status: "설계 완료", note: "출고/입고/피킹 API" },
  { name: "DPS Protocol Agent", endpoint: "ws://localhost:4030/ws/dps", status: "연결 가능", note: "피킹 배치 시뮬레이터" },
  { name: "Print Agent", endpoint: "http://localhost:4040", status: "예정", note: "송장 출력 큐" },
  { name: "PDF Renderer", endpoint: "http://localhost:4050", status: "예정", note: "송장/Picking List 렌더링" },
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

          <div className="p-6 max-sm:p-4">
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
        <Metric label="입고 진행" value="14" sub="검수 6 / 적치 8" icon={PackageCheck} tone="green" />
        <Metric label="가용 재고" value="1,065" sub="할당 173 / 보류 6" icon={Boxes} tone="blue" />
        <Metric label="출고 지시" value="37" sub="재고부족 3건" icon={Truck} tone="amber" />
        <Metric label="피킹 작업" value="132" sub="완료율 43%" icon={ClipboardList} tone="slate" />
      </div>

      <div className="grid grid-cols-[1.4fr_1fr] gap-5 max-xl:grid-cols-1">
        <SectionPanel title="오늘의 운영 큐" action="새로고침">
          <div className="grid gap-2">
            {[
              ["입고 검수 대기", "5건", "A 고객사 신규 입고 지시"],
              ["출고 할당 실패", "3건", "SKU-7780 가용 재고 부족"],
              ["DPS 웨이브 대기", "1건", "WAVE-0518-PM-01"],
              ["송장 출력 대기", "28건", "Print Agent 연결 예정"],
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

        <SectionPanel title="시스템 연결" action="상세">
          <div className="grid gap-3">
            {agentRows.map((agent) => (
              <div key={agent.name} className="flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">{agent.name}</p>
                  <p className="truncate text-xs text-[#6b7780]">{agent.endpoint}</p>
                </div>
                <StatusPill value={agent.status} />
              </div>
            ))}
          </div>
        </SectionPanel>
      </div>
    </div>
  );
}

function ReceivingView() {
  return (
    <SectionPanel title="입고 지시 및 분할 적치" action="입고 생성">
      <DataTable
        columns={["입고번호", "고객사", "SKU", "예정", "검수", "적치", "상태"]}
        rows={receivingRows.map((row) => [
          row.no,
          row.client,
          row.sku,
          row.requested.toLocaleString(),
          row.received.toLocaleString(),
          row.putaway.toLocaleString(),
          <StatusPill key={row.no} value={row.status} />,
        ])}
      />
    </SectionPanel>
  );
}

function InventoryView() {
  return (
    <SectionPanel title="로케이션별 재고" action="CSV">
      <DataTable
        columns={["SKU", "상품명", "로케이션", "가용", "할당", "보류"]}
        rows={inventoryRows.map((row) => [
          row.sku,
          row.name,
          row.location,
          row.available.toLocaleString(),
          row.allocated.toLocaleString(),
          row.hold.toLocaleString(),
        ])}
      />
    </SectionPanel>
  );
}

function OutboundView() {
  return (
    <SectionPanel title="출고 지시 접수" action="지시 등록">
      <DataTable
        columns={["출고번호", "고객사", "라인", "요청일", "상태"]}
        rows={outboundRows.map((row) => [
          row.no,
          row.client,
          row.lines.toLocaleString(),
          row.requestedAt,
          <StatusPill key={row.no} value={row.status} />,
        ])}
      />
    </SectionPanel>
  );
}

function PickingView() {
  return (
    <SectionPanel title="출고 웨이브 및 피킹 작업" action="웨이브 생성">
      <DataTable
        columns={["웨이브", "존", "주문", "작업", "완료", "상태"]}
        rows={pickingRows.map((row) => [
          row.wave,
          row.zone,
          row.orders.toLocaleString(),
          row.tasks.toLocaleString(),
          row.picked.toLocaleString(),
          <StatusPill key={row.wave} value={row.status} />,
        ])}
      />
    </SectionPanel>
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
        <div className="flex gap-2">
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
    <SectionPanel title="실행 단위 프로그램" action="6개">
      <DataTable
        columns={["프로그램", "엔드포인트", "상태", "역할"]}
        rows={agentRows.map((row) => [
          row.name,
          row.endpoint,
          <StatusPill key={row.name} value={row.status} />,
          row.note,
        ])}
      />
    </SectionPanel>
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

function SectionPanel({ title, action, children }: { title: string; action: string; children: React.ReactNode }) {
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

function DataTable({ columns, rows }: { columns: string[]; rows: Array<Array<React.ReactNode>> }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[720px] border-collapse text-left text-sm">
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
            <tr key={rowIndex} className="border-b border-[#edf1f2] last:border-b-0">
              {row.map((cell, cellIndex) => (
                <td key={cellIndex} className="px-3 py-3 text-[#2f3a42]">
                  {cell}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function StatusPill({ value }: { value: string }) {
  const normalized = value.toLowerCase();
  const tone =
    normalized.includes("완료") || normalized.includes("completed") || normalized.includes("연결")
      ? "bg-[#dceee8] text-[#1b5e57]"
      : normalized.includes("부족") || normalized.includes("failed")
        ? "bg-[#f8dddd] text-[#9b2c2c]"
        : normalized.includes("light") || normalized.includes("진행") || normalized.includes("가능")
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
