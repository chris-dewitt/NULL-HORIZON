# ADR-0011: Trusted-development subprocess runners vs blocked public sandbox

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 8

## Context

Epic 8 needs Python and C++ workers with limits, cancellation, retention, and proof that learner code never runs in the API process. A production hardened sandbox is not yet security-reviewed.

## Decision

1. Keep the `ExecutionProvider` boundary (ADR-0008). The API may enqueue jobs and read results; it must not import or exec learner source.
2. Implement a **trusted-development** orchestrator in `backend/runner/` that:
   - Uses a per-job ephemeral workspace
   - Enforces wall-clock timeout, output-size, process, and address-space limits
   - Injects a network guard for Python jobs
   - Supports cancel + retention cleanup
3. Ship `HardenedSandboxProvider` as **explicitly blocked** until security review (`PUBLIC_EXECUTION_ENABLED` remains false).
4. Default API `EXECUTION_PROVIDER=fake` (fixtures only). `local_trusted` selects the runner orchestrator for developer use.

## Alternatives considered

- Require Docker/gVisor for every Epic 8 test — deferred; Compose remains available, but hermetic unit tests use subprocess isolation.
- Enable public hardened execution now — rejected; acceptance requires public deployment stay blocked.

## Consequences

- Runner tests cover timeout, output truncation, workspace isolation, cancel, and retention.
- Production cutover needs a later ADR and security review before unblocking `HardenedSandboxProvider`.

## References

- `docs/PRODUCT_SPEC.md` §19, §32 Epic 8
- `docs/ADR/0008-remote-execution-boundary.md`
