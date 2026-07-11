# NULL HORIZON backend

FastAPI foundation plus trusted-development execution orchestration (Epics 7–8).
Learner-authored code must never execute in the API process.

## Layout

```text
api/       Public HTTP API, models, Alembic
runner/    Execution contracts, queue, workers, orchestrator
shared/    Shared schemas and observability helpers
```

## Local development

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
pytest
uvicorn api.app.main:app --reload
```

Trusted development services:

```bash
docker compose -f ../infra/compose/dev.yml up --build
```

## Execution providers

| `EXECUTION_PROVIDER` | Behavior |
| --- | --- |
| `fake` (default) | Deterministic fixtures; never imports learner source |
| `local_trusted` | Runner orchestrator with ephemeral workspaces and subprocess workers |
| `hardened` / `production` | Blocked until security review (HTTP 503) |

See ADR-0011. Public deployment of a hardened sandbox remains disabled.
