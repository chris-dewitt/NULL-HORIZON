# API

Public API principles and initial endpoints are defined in [PRODUCT_SPEC.md](PRODUCT_SPEC.md) §18 and §24.

## Principles

- Version public endpoints under `/v1`.
- Use Pydantic models and generated OpenAPI.
- Use structured error responses; never return raw internal stack traces.
- Keep learner execution out of the API process.

## Epic 0 surface

The minimal FastAPI shell currently exposes:

- `GET /v1/health`

Additional endpoints (content manifest, anonymous profiles, progress sync, executions) arrive in Epic 7.
