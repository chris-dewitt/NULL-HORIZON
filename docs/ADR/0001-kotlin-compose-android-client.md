# ADR-0001: Kotlin and Jetpack Compose Android client

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline

## Context

NULL HORIZON ships first on Android and needs a maintainable native UI for terminals, editors, maps, and accessibility.

## Decision

Build the primary client in Kotlin with Jetpack Compose, Coroutines, Flow, Hilt, Room, DataStore, WorkManager, Navigation Compose, Retrofit, OkHttp, and Kotlin serialization.

## Alternatives considered

- Flutter or React Native — rejected for first release to keep native Android accessibility and editor control closer to platform APIs.
- XML Views only — rejected; Compose is the specified UI toolkit.

## Consequences

- Android contributors need Kotlin/Compose expertise.
- Shared UI with iOS is deferred.
- Package boundaries in `android-app/` must remain clean even if physical Gradle modules start small.

## References

- `docs/PRODUCT_SPEC.md` §4.1, §17
