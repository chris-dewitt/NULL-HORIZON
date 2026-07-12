# client-core

Cross-client Kotlin shared by `android-app` and `pc-app` as a plain shared
source set (see ADR-0020). Both clients compile these sources with their own
toolchain via `kotlin.srcDir(...)` — there is no separate Gradle module.

Contents: deterministic simulators (terminal, git, sql, service map,
pipeline, mlops, fake execution), mission/objective/hint engines, progression
engine, content models, repository interfaces, and theme tokens.

Rules:

- No `android.*` imports and no Android-only `androidx.*` APIs. The only
  `androidx.compose.*` usage allowed is API surface that exists identically
  in Compose Multiplatform for Desktop.
- No desktop-only APIs (`java.awt`, `javax.swing`).
- Platform persistence (DataStore, files) stays in each client behind the
  interfaces defined here.
- Shared tests under `src/test/kotlin` run in BOTH clients' unit-test tasks.
