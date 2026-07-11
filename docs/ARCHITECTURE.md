# Architecture Overview

This document summarizes the architecture defined in [PRODUCT_SPEC.md](PRODUCT_SPEC.md). It is a map, not a substitute for the specification.

## System boundaries

```text
Android app (Compose)
  -> local simulators (terminal, Git, SQL, pipelines, ML ops)
  -> local progress / content bundles
  -> HTTPS -> FastAPI API
                -> PostgreSQL / Redis
                -> Isolated execution orchestrator (Python / C++ runners)
```

## Non-negotiable boundaries

- The Android UI must not evaluate missions or execute learner code.
- The FastAPI API must not execute learner-authored code in-process.
- Learner execution happens only behind an `ExecutionProvider` interface in an isolated runner.
- Mission content is versioned data under `content/`, not hardcoded application logic.
- Deterministic mission evaluation must not use an LLM.

## Package layout

| Path | Responsibility |
|---|---|
| `android-app/` | Kotlin / Compose client |
| `backend/api/` | Public HTTP API |
| `backend/runner/` | Execution orchestration and workers |
| `backend/shared/` | Shared backend schemas and observability helpers |
| `content/` | Skills, chapters, missions, environments, dialogue |
| `shared/` | Cross-cutting OpenAPI / JSON Schema / fixtures |
| `infra/` | Local Compose, Terraform, policies, images |
| `docs/ADR/` | Architecture decisions |

## Current stage

Epic 0 provides repository standards and minimal compilable shells only. See delivery epics in the product specification §32.
