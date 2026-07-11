# ADR-0018: Release-readiness privacy, deletion, and crash reporting

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 13

## Context

Epic 13 requires privacy policy, data export/deletion, crash reporting with
privacy review, store listing accuracy, accessibility and security assessments,
closed-test and rollback processes, and confirmation that the opening campaign
works without account creation.

## Decision

1. Ship a repository privacy policy (`PRIVACY.md` / `docs/legal/privacy-policy.md`)
   that matches PRODUCT_SPEC §39 and store-listing disclosures for online
   execution.
2. Local player data (profile, progression, settings) can be exported as JSON and
   deleted from Settings without requiring an account.
3. Cloud deletion is available via authenticated API:
   `DELETE /v1/progress` and `DELETE /v1/profiles/me`.
4. Crash reporting is **opt-in**, disabled by default, and implemented as a local
   stub that never transmits source, terminal history, SQL, or secrets. A real
   vendor SDK is not enabled until a follow-up ADR after privacy review sign-off.
5. Analytics collection is **opt-out** (default off for local-only play).
6. Release operations docs cover store assets, content rating, closed test track,
   performance profiling baselines, accessibility audit, security assessment, and
   rollback.

## Alternatives considered

- Enable a third-party crash SDK immediately — rejected until privacy review and
  human approval to publish.
- Require account creation for deletion — rejected; local-only players must be
  able to wipe device data.

## Consequences

- Settings gains privacy/data controls.
- Store listing copy must stay aligned with `docs/store/listing.md`.
- No production store publish from this epic (AGENTS.md / human approval).

## References

- `docs/PRODUCT_SPEC.md` §23.3, §39, §32 Epic 13
