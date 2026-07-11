# ADR-0019: Separate Compose Desktop PC client

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product request for a dedicated PC version

## Context

NULL HORIZON ships first as an Android client (ADR-0001). Operators also need a keyboard-first desktop client that reuses the same mission content, simulators, and progression rules without blocking Android delivery or inventing a second content pipeline.

## Decision

Add a **separate** Compose Desktop JVM client under `pc-app/` that:

1. Reuses the same JSON content bundles produced by `scripts/build_bundle.py`.
2. Ports JVM-safe simulation, mission, and progression engines with the same package contracts as Android.
3. Stores local profile, settings, and progression as JSON files under `~/.null-horizon` by default (not Android DataStore).
4. Keeps Android as the primary store-distributed client; the PC client is a parallel monorepo surface.
5. Does not share a Gradle multiplatform UI module yet — duplication of domain Kotlin is accepted until a shared `:core` extraction ADR is warranted.

## Alternatives considered

- Compose Multiplatform shared UI module with Android + Desktop — deferred; would require invasive Android refactor and delay the dedicated PC surface.
- Electron / web client — rejected for first PC cut to keep Kotlin simulator parity and avoid a second language stack for mission engines.
- Shipping only the Android APK on desktop emulators — rejected; not a first-class PC experience.

## Consequences

- Content authors keep a single YAML source of truth; bundles sync to Android assets and PC classpath resources.
- Domain bug fixes may need dual updates until shared extraction.
- PC packaging uses Compose Desktop native distributions (DMG/MSI/DEB) for local installers; store publish remains out of scope without human approval.
- Security boundaries unchanged: no learner code in the API process; PC uses the same fake/local simulators offline.

## References

- `docs/PRODUCT_SPEC.md` §4.1, §17
- ADR-0001 Kotlin Compose Android client
- ADR-0002 Monorepo
- ADR-0004 YAML content bundles
