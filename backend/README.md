# NULL HORIZON backend

FastAPI foundation for Epic 7. Learner-authored code must never execute in this process.

## Layout

```text
api/       Public HTTP API, models, Alembic
runner/    Execution contracts and future workers
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

## Security boundary

`EXECUTION_PROVIDER` defaults to `fake`. The fake provider returns deterministic fixtures and never imports learner source. Real execution belongs in isolated runners (Epic 8), not in `api/`.
