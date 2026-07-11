# NULL HORIZON backend

Minimal FastAPI shell for Epic 0. Learner-authored code must never execute in this process.

## Layout

```text
api/       Public HTTP API
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

## Security boundary

`EXECUTION_PROVIDER` defaults to `fake`. Real execution belongs in isolated runners (Epic 8), not in `api/`.
