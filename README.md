# Warehouse Ops Suite

3PL 물류센터의 입고, 적치, 재고, 출고 지시, 피킹 웨이브, DPS 장비 연동, PDF 출력, 로컬 프린트 에이전트를 하나의 운영 흐름으로 연결한 WMS 포트폴리오 프로젝트입니다.

이 레포는 실무 도메인 경험을 바탕으로 한 클린룸 재설계 프로젝트입니다. 회사 소스 코드, 내부 패키지명, DTO, API 경로, DB 스키마, 고객사명, 센터명, 실제 운영 로그는 사용하지 않습니다.

## Portfolio Positioning

이 레포는 취업용 포트폴리오에서 **물류 WMS 도메인 모델링과 현장 시스템 연동 설계 역량**을 보여주기 위한 프로젝트입니다.

| 평가 포인트 | 이 프로젝트에서 보여주는 내용 |
| --- | --- |
| 도메인 모델링 | 입고, 적치, 재고, 출고, 피킹 상태 전이를 업무 단위로 분리 |
| 운영 화면 설계 | 운영자가 다음 행동을 판단할 수 있는 관리자 UI 구성 |
| 현장 연동 경계 | PDA, DPS Protocol Agent, Print Agent를 중앙 API와 분리 |
| Clean-room 재구성 | 회사 코드와 데이터를 쓰지 않고 실무 도메인 경험을 공개 가능한 형태로 일반화 |
| 멀티 애플리케이션 구성 | Admin Web, API Server, PDA, Agent, PDF Renderer를 역할별로 구성 |

## Applications

| Path | Role | Stack |
| --- | --- | --- |
| `apps/admin-web` | WMS 운영 관리자 화면 | React, TypeScript, Vite |
| `apps/pda-app` | 현장 PDA 작업 앱 | Flutter, Dart |
| `apps/print-agent` | 로컬 프린터 브리지 | Kotlin, Ktor |
| `apps/dps-protocol-agent` | DPS 장비 프로토콜 시뮬레이터 | Kotlin, Ktor WebSocket |
| `services/api-server` | WMS 도메인 API | Kotlin, Spring Boot, JPA, Flyway |
| `services/pdf-renderer` | 송장/피킹리스트 문서 렌더러 | Node.js, TypeScript, Playwright |

## Domain Scope

- 3PL 고객사별 재고 소유 구조
- 입고 지시, PDA 검수, 로케이션 분할 적치
- 출고 지시 접수, 중복 지시 방지, 재고 할당
- 피킹 웨이브 생성과 PickingTask 관리
- DPS 셀 점등/완료 이벤트 시뮬레이션
- 피킹리스트/PDF 생성과 로컬 출력 큐
- 중앙 서비스와 현장 로컬 에이전트 분리

## Repository Layout

```text
apps/
services/
packages/
infra/
docs/
blog/
  article.md
  images/
.portfolio/
  manifest.json
scripts/
  build-portfolio-package.mjs
```

`docs/`는 설계, 운영, API 문서를 보관합니다. `blog/`는 포트폴리오 허브에 발행되는 게시물 본문과 이미지를 보관합니다.

## Portfolio Blog Package

포트폴리오 허브는 이 레포의 `blog/` 패키지를 읽는 것이 아니라, CI가 만든 S3 업로드용 패키지를 읽습니다.

```text
blog/article.md
blog/images/*
.portfolio/manifest.json
-> dist/portfolio-package/
-> S3 portfolio-feed/warehouse-ops-suite/
-> portfolio-hub
```

로컬에서 패키지를 생성하려면 다음 명령을 실행합니다.

```bash
pnpm portfolio:package
```

생성 결과:

```text
dist/portfolio-package/
├─ manifest.json
├─ article.md
└─ images/
```

GitHub Actions의 `Publish Portfolio Package` 워크플로우는 `main` 브랜치 push 때 이 패키지를 만들고, AWS Secrets가 설정되어 있으면 S3 업로드까지 수행합니다. Secrets가 없으면 artifact만 생성하고 S3 업로드는 건너뜁니다.

## Local Verification

```bash
pnpm typecheck
pnpm portfolio:package
```

전체 애플리케이션 검증은 프로젝트 상태에 따라 아래 명령을 사용합니다.

```bash
pnpm verify
```

## Documents

- [Architecture](docs/architecture.md)
- [Domain Flow](docs/domain-flow.md)
- [API Contract](docs/api-contract.md)
- [Product Manual](docs/product-manual.md)
- [Portfolio Blog Article](blog/article.md)
