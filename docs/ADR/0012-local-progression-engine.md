# ADR-0012: Local progression engine with DataStore snapshot and sync merge

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 9

## Context

Epic 9 requires skill mastery, rank, deterministic rewards, debrief, review
recommendations, and offline sync that never loses completion. ADR-0003 deferred
Room entities; DataStore already stores completed mission IDs.

## Decision

1. Keep a pure **ProgressionEngine** (Kotlin + mirrored Python) that applies a
   mission completion event to an immutable progression snapshot:
   - Mission completion is monotonic.
   - Best assistance level keeps the least-assisted success (`min` hint level).
   - Mastery evidence is event-based and deduplicated by `event_id`.
   - Rewards are a set union.
   - Clearance points are awarded once per mission id.
   - Rank is derived from clearance points (capability thresholds), not grind XP.
2. Persist the snapshot as versioned JSON in DataStore (mirrors §22.1 shapes).
   Room remains a follow-up when query volume justifies it.
3. Queue pending sync operations locally; backend `/v1/progress/sync` merges with
   the same rules and stores `skill_evidence` + `reward_unlocks`.
4. Debrief and skill-map UI read the local snapshot; review recommendations come
   from the engine (stale or under-practiced skills).

## Alternatives considered

- Introduce Room immediately — deferred; JSON snapshot matches current DI and
  keeps JVM unit tests hermetic without Android SQLite.
- Server-authoritative rewards — rejected; offline-first requires local apply.

## Consequences

- Re-completing a mission is idempotent for clearance, rewards, and evidence.
- Assisted completions still grant evidence but do not increment unassisted counts.
- Public numerical “IQ” scores remain forbidden; only mastery states and rank titles.

## References

- `docs/PRODUCT_SPEC.md` §10, §13, §22, §23, §32 Epic 9
- `docs/ADR/0003-local-first-profile.md`
