# ADR-0005: Simulated terminal instead of embedded shell

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 3

## Context

Linux/Bash missions need a terminal experience on Android without exposing the device filesystem or a real shell.

## Decision

Implement a deterministic in-memory terminal simulator with an explicit command registry and a mission-scoped virtual filesystem. Epic 3 supports `pwd`, `ls`, `cd`, `cat`, and `grep`. Unsupported syntax is rejected with clear errors. The simulator never reads or writes Android storage outside the mission content bundle used to seed the VFS.

## Alternatives considered

- Embed a real shell / userspace — rejected; unsafe and non-deterministic on mobile.
- Remote shell service — rejected for offline introductory missions and expands the attack surface.

## Consequences

- Command semantics are documented and unit-tested.
- Mission environments declare a virtual filesystem tree in content data.
- Later epics can extend the registry (pipes, redirection, more commands) without changing the isolation model.

## References

- `docs/PRODUCT_SPEC.md` §9.2, §20.1–20.2, §32 Epic 3, §36 Task 5
