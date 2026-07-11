# ADR-0007: Local SQLite mission databases

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 5

## Context

SQL missions need a real relational engine for SELECT (and later DML) without exposing the application DataStore/Room profile database to the learner console. Unit tests must run on the JVM without an Android device.

## Decision

1. Each SQL mission declares an isolated database in content (`environment.databases`) with an immutable `seed_sql` script and a `policy` (Epic 5: `select_only`).
2. The mission SQL engine uses **JDBC SQLite** (`org.xerial:sqlite-jdbc`) against an in-memory database created from the seed. This keeps the simulator Android-API-free so unit tests stay deterministic on the JVM.
3. Learner statements pass through a policy gate that allows a single `SELECT` / `WITH … SELECT` and rejects `ATTACH`, unsafe `PRAGMA`, DML/DDL, and multi-statement payloads.
4. Session UI state stores schema metadata and the last result set only; the live connection lives in the mission state machine and is rebuilt from seed on reset.

## Alternatives considered

- Android `SQLiteDatabase` / Room for mission DBs — rejected for Epic 5 because JVM unit tests cannot exercise them without Robolectric, and Room is reserved for application data.
- Pure in-memory fake SQL — rejected; joins/aggregation semantics would drift from real SQLite.
- Shipping binary `.sqlite` seeds only — deferred; `seed_sql` is easier to review and validate. `seed_file` remains a schema extension point for later epics.

## Consequences

- Mission databases never share a connection or file with application preferences.
- Reset always reinstalls from `seed_sql`.
- Objective engines compare result sets with ordered or unordered semantics.
- Adding DML policies later reuses the same installer and session model.

## References

- `docs/PRODUCT_SPEC.md` §9.4, §32 Epic 5, §36 Task 7, §37 item 7
