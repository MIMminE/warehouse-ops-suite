# Release Strategy

## 목적

`warehouse-ops-suite`는 하나의 Git 코드베이스 안에 여러 실행 단위가 있는 모노레포다.
하지만 실제 운영에서는 각 프로그램의 배포 위치와 릴리즈 주기가 다르다.

- `admin-web`: 운영자 웹, 변경 빈도가 높고 웹 배포 단위로 릴리즈
- `api-server`: 핵심 WMS API, DB 마이그레이션과 함께 신중하게 릴리즈
- `pda-app`: 현장 Android 앱, 작업자 단말 배포 주기를 고려해 릴리즈
- `print-agent`: 현장 PC 로컬 에이전트, 센터별 설치/업데이트 주기 관리
- `dps-protocol-agent`: 현장 장비 연동 에이전트, 장비 프로토콜 변경과 독립 릴리즈
- `pdf-renderer`: 송장/피킹리스트 렌더링 서비스, 문서 양식 변경 중심 릴리즈

따라서 이 레포는 **한 코드베이스, 다중 릴리즈 트랙**을 기본 전략으로 둔다.

## 릴리즈 단위

| Component | Path | Release artifact | 주기 |
| --- | --- | --- | --- |
| Admin Web | `apps/admin-web` | 정적 빌드 산출물 | 수시 |
| API Server | `services/api-server` | Spring Boot jar 또는 container image | 계획 릴리즈 |
| PDA App | `apps/pda-app` | Android APK/AAB | 현장 배포 단위 |
| Print Agent | `apps/print-agent` | JVM jar 또는 설치 패키지 | 센터별 릴리즈 |
| DPS Agent | `apps/dps-protocol-agent` | JVM jar 또는 설치 패키지 | 장비 연동 변경 시 |
| PDF Renderer | `services/pdf-renderer` | Node service artifact 또는 container image | 양식 변경 시 |

## CI 원칙

1. PR에서는 변경된 컴포넌트만 빠르게 검증한다.
2. 공통 파일이 바뀌면 관련 컴포넌트를 함께 검증한다.
3. API Server는 Flyway/JPA smoke test를 항상 포함한다.
4. 로컬 에이전트는 서버 기동 가능성보다 우선 빌드와 테스트를 검증한다.
5. Admin Web은 lint와 production build를 기본 검증으로 둔다.
6. PDA App은 `flutter analyze`와 `flutter test`를 기본 검증으로 둔다.

## 릴리즈 원칙

릴리즈는 자동 배포보다 먼저 **수동 승인 기반 패키징**으로 시작한다.

```text
workflow_dispatch
-> component 선택
-> version 입력
-> 해당 component만 build/test
-> artifact 업로드
-> GitHub Release 또는 배포 환경으로 전달
```

이후 실제 운영 배포를 붙일 때는 다음처럼 나눈다.

- Web/Server: AWS 기반 자동 배포
- PDA: APK/AAB 생성 후 MDM 또는 내부 배포 채널
- Print/DPS Agent: 센터별 설치 패키지 배포
- PDF Renderer: API Server와 별도 서비스로 container 배포

## 버전 규칙

컴포넌트별 버전을 독립적으로 둔다.

```text
admin-web@0.3.0
api-server@0.5.0
pda-app@0.2.1
print-agent@0.1.3
dps-protocol-agent@0.1.2
pdf-renderer@0.1.1
```

Git tag는 다음 형식을 사용한다.

```text
admin-web/v0.3.0
api-server/v0.5.0
pda-app/v0.2.1
print-agent/v0.1.3
dps-protocol-agent/v0.1.2
pdf-renderer/v0.1.1
```

## 배포 리스크 관리

- `api-server` 배포 전 DB 마이그레이션 영향 확인
- `pda-app`은 현장 작업 중 강제 업데이트를 피하고 센터별 배포 창구 운영
- `print-agent`는 프린터 드라이버/OS 차이를 고려해 점진 배포
- `dps-protocol-agent`는 장비 프로토콜 변경 시 시뮬레이터 검증 후 현장 적용
- `admin-web`은 API 호환성을 확인한 뒤 배포

## 포트폴리오에서 강조할 점

이 프로젝트는 단순히 여러 앱을 한 레포에 넣은 것이 아니라,
현장 프로그램들의 릴리즈 주기가 다르다는 전제를 두고 CI/CD 경계를 설계한다.

특히 로컬 에이전트와 PDA 앱은 서버와 배포 방식이 다르기 때문에,
컴포넌트별 검증, 패키징, 릴리즈 승인을 분리하는 구조가 중요하다.

