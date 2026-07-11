# ADR-0002: Monorepo

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline

## Context

The product spans Android, FastAPI, content bundles, shared schemas, and infrastructure. Cross-cutting contracts must stay aligned.

## Decision

Use a single monorepo containing `android-app/`, `backend/`, `content/`, `shared/`, `infra/`, `scripts/`, and `docs/`.

## Alternatives considered

- Polyrepo per component — rejected for early delivery because schema and contract drift would be costly.
- Content-only separate repository — deferred until publication workflow requires stronger separation.

## Consequences

- One CI entrypoint can validate contracts across client, API, and content.
- Contributors must keep commits focused despite the shared tree.
- Generated artifacts must be clearly identified.

## References

- `docs/PRODUCT_SPEC.md` §4.2, §29
