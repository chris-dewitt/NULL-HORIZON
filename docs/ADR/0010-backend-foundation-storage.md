# ADR-0010: Backend foundation storage and fake execution adapter

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 7

## Context

Epic 7 needs PostgreSQL models, Redis, and an execution job API while keeping learner code out of the API process. CI and `./scripts/check.sh` must remain runnable without requiring live Docker services.

## Decision

1. Use SQLAlchemy 2.x models and Alembic migrations targeting PostgreSQL in trusted development (`infra/compose/dev.yml`).
2. Default automated tests to an in-memory SQLite database and an in-process Redis substitute when `DATABASE_URL` / `REDIS_URL` are unset.
3. Expose anonymous bearer tokens for profile-scoped progress and execution routes.
4. Honor `Idempotency-Key` (and body `idempotency_key`) for progress sync and execution submission via the Redis adapter.
5. Implement `FakeExecutionProvider` as a deterministic fixture adapter that never imports, compiles, or executes learner source (ADR-0008).

## Alternatives considered

- Require Docker for every test run — rejected; slows CI and local agent loops.
- Skip Redis until Epic 8 — rejected; Epic 7 acceptance requires a Redis adapter and idempotency.

## Consequences

- Developers use Compose for Postgres/Redis parity.
- Unit tests stay hermetic.
- Epic 8 can replace the fake provider without changing HTTP contracts.

## References

- `docs/PRODUCT_SPEC.md` §18, §19.2, §22.2, §32 Epic 7, §36 Task 9
- `docs/ADR/0008-remote-execution-boundary.md`
