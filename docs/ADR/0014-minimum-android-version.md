# ADR-0014: Minimum supported Android version (provisional)

- Status: Accepted (provisional)
- Date: 2026-07-11
- Deciders: Repository bootstrap

## Context

The Android shell needs a pinned `minSdk` for Epic 0 compilation. Final store policy remains an open product question.

## Decision

Set `minSdk = 26` (Android 8.0) provisionally for the Epic 0 Compose shell. Revisit before public launch based on analytics, accessibility requirements, and Play policy.

## Alternatives considered

- minSdk 24 — deferred; widens device reach but increases maintenance for modern APIs.
- minSdk 29+ — deferred until device-support data justifies dropping older versions.

## Consequences

- Compose and modern Jetpack libraries are usable without extensive legacy shims.
- Final minimum version must be confirmed in release readiness work.

## References

- `docs/PRODUCT_SPEC.md` §37, §41
- `android-app/gradle/libs.versions.toml`
