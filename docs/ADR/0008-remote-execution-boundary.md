# ADR-0008: Remote Python and C++ execution boundary

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline

## Context

Unrestricted learner-authored Python and C++ cannot safely run on-device or inside the API process.

## Decision

Place learner code behind an `ExecutionProvider` interface. The API accepts execution jobs and never runs learner code in-process. Implementations progress from `FakeExecutionProvider` to trusted local Docker (development only) to a hardened production sandbox.

## Alternatives considered

- In-process evaluation inside FastAPI — rejected; violates the security boundary.
- On-device unrestricted interpreters — rejected for version 1 due to isolation and binary-size constraints.
- LLM-based grading of code — rejected; mission evaluation must remain deterministic.

## Consequences

- Online missions require network and an execution service.
- API and runner remain separate trust domains.
- Security tests are mandatory for runner changes.

## References

- `docs/PRODUCT_SPEC.md` §16, §19, §35
