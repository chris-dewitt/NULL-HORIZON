# ADR-0003: Local-first profile and optional sync

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 1 shell

## Context

Chapter 0 must be playable without an account. Progress and preferences need a local source of truth before optional cloud sync exists.

## Decision

- Keep the local profile authoritative while offline.
- Epic 1 stores the local operator display name and accessibility preferences in DataStore.
- Room entities for mission progress, mastery, and sync queues arrive with later progression epics.
- Cloud sync remains optional and is not required to launch or navigate the shell.

## Alternatives considered

- Account-gated first launch — rejected; conflicts with offline-first onboarding.
- Room for display-name-only profile in Epic 1 — deferred; DataStore is sufficient for shell preferences until progress entities exist.

## Consequences

- No authentication is required for Epic 1 screens.
- Profile and accessibility state survive process death via DataStore.
- Later sync merge rules must treat local completion as monotonic.

## References

- `docs/PRODUCT_SPEC.md` §4.1, §17.3, §23, §32 Epic 1
