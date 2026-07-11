# ADR-0004: YAML authoring compiled to JSON bundles

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 2

## Context

Mission content must be reviewable, schema-validated, and loadable by the Android client without embedding mission-specific UI logic.

## Decision

- Author skills, chapters, missions, dialogue, and related content as YAML under `content/`.
- Compile YAML into a versioned JSON content bundle with checksums.
- Validate against JSON Schema before bundle publication.
- Ship a starter bundle in Android assets; later epics may download signed bundles.

## Alternatives considered

- Author directly in JSON — rejected; YAML comments and writer ergonomics matter for curriculum work.
- Parse YAML on-device — rejected for Epic 2; compiled JSON keeps the client simpler and validation on the build path.

## Consequences

- Invalid content fails in `scripts/validate_content.py` / `scripts/build_bundle.py`.
- Android loads only compiled bundle JSON.
- Content changes require a rebuild to update assets.

## References

- `docs/PRODUCT_SPEC.md` §21, §28, §32 Epic 2
