# AGENTS.md

## Project Context

Warehouse Ops Suite is a clean-room WMS portfolio project.

- This file lives at the project root and applies to every app, service, package, and document in this workspace.
- Do not copy company source code, package names, DTO names, API paths, database schemas, customer names, center names, logs, or internal configuration values.
- Generalize domain experience into new names, new contracts, and new implementation details.
- Keep portfolio documentation explicit about the clean-room redesign principle.

## Git Commit Convention

Use a lightweight Conventional Commits style with Korean subject lines.

```text
<type>: <한글 커밋 메시지>

<간단한 본문>
```

Example:

```text
fix: PDA 피킹 완료 수량 검증 오류 수정

분할 피킹 완료 시 남은 수량보다 큰 값이 입력될 수 있던 문제를 막고,
완료 수량 계산 기준을 작업 상세 기준으로 정리했다.
```

## Commit Types

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 추가 또는 수정
- `refactor`: 동작 변경 없는 구조 개선
- `test`: 테스트 추가 또는 수정
- `chore`: 빌드, 설정, 의존성, 기타 작업
- `style`: 포맷팅, 스타일 수정
- `perf`: 성능 개선
- `ci`: CI/CD 설정 변경

## Subject Rules

- Subject는 한국어로 작성한다.
- Subject는 50자 내외로 짧게 작성한다.
- 끝에 마침표를 붙이지 않는다.
- 무엇을 했는지보다 사용자가 이해할 수 있는 변경 결과를 우선한다.

Good:

```text
feat: 출고 웨이브 생성 API 추가
fix: 송장 출력 실패 상태가 갱신되지 않던 문제 수정
docs: 클린룸 재설계 원칙 문서화
```

Avoid:

```text
fix: 버그 수정
feat: 기능 개발
chore: 이것저것 수정
```

## Body Rules

- Body는 필요할 때만 작성한다.
- Body는 1~3문장 정도로 간단하게 작성한다.
- 변경 이유, 판단 근거, 영향 범위를 적는다.
- 회사 내부 정보, 실제 고객사명, 운영 로그, 내부 API 경로는 쓰지 않는다.

Body example:

```text
출고 웨이브 생성 시 작업 수량과 피킹 대상 로케이션을 함께 기록하도록 했다.
후속 PDA 피킹 흐름에서 작업 단위 상태 전이를 검증하기 위한 기반이다.
```

## Scope

Scope는 필요한 경우에만 사용한다.

```text
feat(api-server): 출고 웨이브 생성 API 추가
fix(print-agent): 출력 큐 실패 상태 갱신
docs(portfolio): 프로젝트 소개 문구 정리
```

Recommended scopes:

- `admin-web`
- `pda-app`
- `print-agent`
- `dps-agent`
- `api-server`
- `pdf-renderer`
- `contracts`
- `docs`
- `infra`

## Branch Naming

Use short English branch names.

```text
feat/outbound-wave
fix/print-job-status
docs/git-convention
chore/project-setup
```

## Pull Request Notes

When opening a PR or writing a change summary, include:

- 변경 요약
- 검증 방법
- 남은 작업 또는 의도적으로 제외한 범위

Example:

```text
## 변경 요약
- 출고 웨이브 생성 API 골격 추가
- 피킹 작업 상태 enum 정의

## 검증 방법
- gradle :services:api-server:test

## 남은 작업
- PDA 피킹 완료 API 연결
```

## Agent Working Rules

- Prefer small, reviewable commits.
- Keep changes scoped to the requested app, service, or document.
- Update docs when architecture, setup, or portfolio positioning changes.
- Do not introduce real company data into tests, fixtures, examples, or screenshots.
- If a change requires generated files, keep generated output minimal and explain why it is needed.
