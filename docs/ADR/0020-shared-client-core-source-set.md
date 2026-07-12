# ADR-0020: Shared client-core Kotlin source set

- Status: Accepted
- Date: 2026-07-12
- Deciders: Follow-up to the duplication accepted in ADR-0019

## Context

ADR-0019 shipped the Compose Desktop client by copying the JVM-safe domain
layer from the Android app and explicitly accepted that duplication "until a
shared `:core` extraction ADR is warranted." That point has arrived: 40 files
under `com.nullhorizon.app` existed twice (simulators, mission/objective/hint
engines, progression engine, content models, repository interfaces, theme
tokens), 39 byte-identical and one already drifting. Every engine fix had to
be made twice, and the PC copy ran almost none of the engine tests.

## Decision

Extract the duplicated Kotlin into a **shared source set** at
`shared/client-core/src/{main,test}/kotlin`, included by both clients via
`kotlin.srcDir(...)` in their existing Gradle builds:

1. Each client compiles the shared sources with its own toolchain (AGP 9
   built-in Kotlin for Android, Kotlin JVM + Compose Multiplatform for PC).
   There is no new Gradle module, plugin set, or publishing step.
2. Shared code must stay platform-pure: no `android.*`/`androidx.*` (other
   than the `androidx.compose.*` APIs available identically on Desktop) and
   no `java.awt`/desktop-only APIs. Platform persistence stays in each app
   (`DataStoreProgressionRepository` on Android, file repositories on PC)
   behind the shared interfaces.
3. Shared engine and simulator tests live in the shared test source set and
   run in **both** clients' test tasks, so JVM/Android runtime parity is
   verified on every build.
4. The single divergent file (`ProgressionRepository.kt`) was reconciled by
   keeping the interface shared and moving the Android DataStore
   implementation to its own Android-only file.

## Alternatives considered

- **Separate Gradle module via composite build (`includeBuild`)** — cleaner
  namespacing, but couples the two builds to a single Kotlin/serialization
  plugin version and adds cross-build plugin-classpath risk; the two clients
  intentionally pin different toolchains (AGP built-in Kotlin vs. JetBrains
  Kotlin JVM). Revisit if a third consumer appears or the builds converge.
- **Kotlin Multiplatform module** — overkill while both targets are JVM;
  warranted only if iOS/web targets are added.
- **Copy-sync script plus CI drift check** — treats the symptom; every fix
  would still land twice.

## Consequences

- One source of truth: engine and simulator fixes land once and reach both
  clients; the engine test suite (16 files) now runs on the PC toolchain too.
- Shared sources are compiled twice (once per client). Build time cost is
  negligible at current size.
- IDE ergonomics: both projects index the shared directory; edits from either
  project affect both (that is the point).
- The shared source set must not grow platform dependencies; reviewers should
  reject `import android.*` or Android-only `androidx.*` in
  `shared/client-core`.

## References

- ADR-0019 Compose Desktop PC client (accepted the duplication this removes)
- ADR-0001 Kotlin Compose Android client
- `docs/PRODUCT_SPEC.md` §17 (Android modules), §4.2
