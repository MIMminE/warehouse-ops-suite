# Warehouse Ops Suite 포트폴리오 완성 보고서

## 1. 프로젝트 목적

Warehouse Ops Suite는 3PL 물류 운영 흐름을 포트폴리오용으로 재설계한 WMS 계열 시스템이다.
회사 코드, 내부 패키지명, 실제 고객사 데이터, 운영 로그를 사용하지 않고 입고, 적치, 재고, 출고 지시, 피킹 웨이브, DPS 장비 연동, PDF 출력, 로컬 프린트 에이전트까지 하나의 시연 흐름으로 구성했다.

핵심 목표는 단순 CRUD가 아니라 여러 실행 단위가 HTTP/WebSocket 기반으로 연결되는 운영 시스템을 보여주는 것이다.

## 2. 전체 구성

| 실행 단위 | 경로 | 역할 | 현재 상태 |
| --- | --- | --- | --- |
| Admin Web | `apps/admin-web` | 운영자용 웹 콘솔 | 포트폴리오 시연 가능 |
| API Server | `services/api-server` | WMS 핵심 도메인/API | 포트폴리오 시연 가능 |
| DPS Protocol Agent | `apps/dps-protocol-agent` | DPS 장비 프로토콜 시뮬레이터 | 포트폴리오 시연 가능 |
| Print Agent | `apps/print-agent` | 로컬 프린터 큐/출력 에이전트 | 포트폴리오 시연 가능 |
| PDF Renderer | `services/pdf-renderer` | 송장/피킹리스트 PDF 렌더링 | 포트폴리오 시연 가능 |
| PDA App | `apps/pda-app` | 입고/적치 현장 작업 앱 | MVP 화면 시연 가능 |

## 3. 구현된 주요 업무 흐름

### 3.1 입고/적치

- 고객사별 입고 지시와 SKU별 입고 상세를 관리한다.
- PDA 현장 작업을 전제로 검수 수량과 적치 수량을 분리했다.
- 하나의 입고 상세를 여러 로케이션에 나누어 적치할 수 있는 `PutawayTask` 구조를 제공한다.
- 적치 완료 시 재고 가용 수량이 증가하는 흐름을 API 서버 도메인에 반영했다.

### 3.2 재고/로케이션

- 고객사, 창고, 로케이션, SKU 기준 재고를 관리한다.
- 가용 수량, 할당 수량, 보류 수량을 분리해 출고 할당과 운영 조회가 가능하도록 했다.
- Admin Web에서 로케이션별 재고 상태를 그래픽 형태로 확인할 수 있다.

### 3.3 출고 지시/할당

- API, CSV, EDI, 수기 입력 등 다양한 출고 지시 유입 방식을 고려한 Intake Port 구조를 두었다.
- 고객사, 창고, SKU 존재 검증과 중복 출고 지시 번호 방지 흐름을 제공한다.
- 출고 지시 라인 기준으로 가용 재고를 할당하고, 재고 부족 예외를 분리했다.

### 3.4 피킹 웨이브/DPS

- 할당된 출고 지시 라인을 피킹 웨이브로 묶고 PickingTask를 생성한다.
- Admin Web에서 웨이브 상세를 클릭하면 하위 송장/작업 라인을 확인할 수 있다.
- DPS 대상 웨이브를 선택해 API Server에서 DPS Protocol Agent로 WebSocket 메시지를 전송한다.
- DPS Agent는 셀 점등, 배치 시작, 피킹 완료 이벤트를 시뮬레이션한다.

### 3.5 PDF/프린트

- PDF Renderer는 송장과 피킹리스트 문서를 HTML/PDF로 렌더링한다.
- API Server는 선택한 피킹 웨이브의 피킹리스트 출력 작업을 생성한다.
- Print Agent는 로컬 프린터 목록과 출력 큐를 제공하고, 출력 요청을 `QUEUED` 또는 `FAILED` 상태로 관리한다.
- Admin Web에서 피킹리스트 출력 요청을 실행하고 출력 작업 상태를 조회할 수 있다.

## 4. 시연 시나리오

1. Docker Compose로 PostgreSQL/Redis를 실행한다.
2. API Server를 `local` profile로 실행한다.
3. Admin Web을 실행하고 운영 현황 대시보드를 확인한다.
4. 출고 지시 화면에서 고객사별 출고 내역과 송장 상세를 확인한다.
5. 피킹 웨이브 화면에서 `WAVE-0518-PM-01`을 선택한다.
6. `DPS 전송`을 눌러 DPS Agent에 피킹 배치를 전송한다.
7. DPS 모니터에서 점등된 셀과 작업 상태를 확인한다.
8. `피킹리스트 출력`을 눌러 Print Agent 출력 큐에 작업을 등록한다.
9. 시스템 연결 화면에서 API Server, PDF Renderer, DPS Agent, Print Agent 구성을 설명한다.
10. PDA App 화면으로 입고/검수/적치가 현장 단말에서 처리되는 컨셉을 보여준다.

## 5. 검증 결과

| 항목 | 명령/방식 | 결과 |
| --- | --- | --- |
| Admin Web build | `pnpm --filter @warehouse/admin-web build` | 성공 |
| Admin Web lint | `pnpm --filter @warehouse/admin-web lint` | 성공 |
| API/Print Agent compile | `gradle --no-daemon :services:api-server:compileKotlin :apps:print-agent:compileKotlin` | 성공 |
| DPS 전송 | `POST /api/outbound-waves/3/dispatch-dps` | `PICKING_BATCH_ACCEPTED` |
| Print Agent 큐 등록 | `POST /api/outbound-waves/3/print-jobs/picking-list` | `QUEUED` |

## 6. 포트폴리오에서 강조할 점

- 3PL 구조를 반영해 고객사별 위탁 상품, 출고 지시, 재고, 피킹 흐름을 분리했다.
- Web, API, PDA, 로컬 에이전트, 장비 프로토콜, PDF 렌더러를 하나의 업무 흐름으로 연결했다.
- 단순 화면 구현이 아니라 상태 전이, 재고 수량 검증, 중복 방지, 에이전트 실패 처리, 출력 큐를 고려했다.
- AWS 배포를 염두에 두고 환경변수 기반 endpoint 설정과 Docker 로컬 인프라 구성을 분리했다.
- 실제 회사 자산을 사용하지 않고 도메인 경험을 일반화해 재설계했다.

## 7. 현재 완성도 평가

포트폴리오 제출/면접 시연 기준으로는 사용할 수 있는 상태다.
다만 상용 수준의 완성도와 비교하면 다음 항목은 의도적으로 MVP 범위로 남겨두었다.

- 사용자 인증/권한
- 실제 택배사 송장 API
- 실제 프린터 드라이버 제어
- PDA의 실기기 바코드 스캐너 연동
- 운영 배포용 CI/CD, 모니터링, 알림

이 항목들은 현재 프로젝트의 부족함이라기보다, 포트폴리오 범위와 실서비스 범위를 구분해 설명할 수 있는 확장 포인트다.
