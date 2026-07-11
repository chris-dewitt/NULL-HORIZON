# ADR-0009: Client-side fake execution fixtures for editor missions

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 6

## Context

Epic 6 needs a Python editor and test console before the backend execution API (Epic 7) and Android remote repository (Epic 10) exist. Learners must edit starter files, submit, and see deterministic pass/fail results offline.

## Decision

1. Keep the `ExecutionProvider` boundary from ADR-0008.
2. For Epic 6, ship a **client-side `FakeExecutionProvider`** that selects deterministic fixture results by matching editable workspace file contents declared in mission content.
3. Do not interpret or execute learner Python on-device. Fixtures are authored data, not a runtime.
4. Remote/trusted runners remain future providers behind the same result model.

## Alternatives considered

- Block editor missions until Epic 7/10 — rejected; Task 8 requires Pressure Threshold now.
- On-device CPython — rejected for version 1 isolation and size (ADR-0008).
- LLM grading — rejected; evaluation must stay deterministic.

## Consequences

- Mission YAML declares workspace files plus fake fixtures.
- UI can exercise success and failure flows without network.
- Epic 10 can swap the provider implementation without changing the test-console model.

## References

- `docs/PRODUCT_SPEC.md` §9.5–9.6, §19.4, §32 Epic 6, §36 Task 8
- `docs/ADR/0008-remote-execution-boundary.md`
