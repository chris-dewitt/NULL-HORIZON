# API

Public API principles and initial endpoints are defined in [PRODUCT_SPEC.md](PRODUCT_SPEC.md) §18 and §24.

## Principles

- Version public endpoints under `/v1`.
- Use Pydantic models and generated OpenAPI.
- Use structured error responses; never return raw internal stack traces.
- Keep learner execution out of the API process.

## Epic 7–9 surface

- `GET /v1/health`
- `GET /v1/content/manifest`
- `GET /v1/content/bundles/{bundle_id}`
- `POST /v1/profiles/anonymous`
- `DELETE /v1/profiles/me` (Bearer) — deletes profile and owned cloud data
- `GET /v1/progress` (Bearer) — missions, skill mastery, rewards, rank, review recommendations
- `PUT /v1/progress/missions/{mission_id}` (Bearer)
- `POST /v1/progress/sync` (Bearer + Idempotency-Key) — merges missions, skill evidence, rewards
- `DELETE /v1/progress` (Bearer) — deletes cloud progress; keeps profile
- `POST /v1/executions` (Bearer + Idempotency-Key; provider via `EXECUTION_PROVIDER`)
- `GET /v1/executions/{execution_id}` (Bearer)
- `DELETE /v1/executions/{execution_id}` (Bearer)

`EXECUTION_PROVIDER` defaults to `fake`. `local_trusted` uses `backend/runner` workers outside the API trust model. `hardened` returns 503 until security review (ADR-0011).

Progress sync merge rules (ADR-0012): mission completion is monotonic, least-assisted hint level wins, evidence events dedupe by `event_id`, rewards are a set union, clearance is awarded once per mission.

OpenAPI is served at `/openapi.json` and mirrored to `shared/openapi/openapi.json` by tests.

## Local services

```bash
docker compose -f infra/compose/dev.yml up --build
```

Automated tests use in-memory SQLite and an in-process Redis substitute when `DATABASE_URL` / `REDIS_URL` are unset (ADR-0010).
