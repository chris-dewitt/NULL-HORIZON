# ADR-0013: Deterministic service-map, pipeline, and ML-ops simulators

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 10

## Context

Epic 10 needs data-engineering and ML-infrastructure missions without real cloud
resources. Spec §9.7 / §20.4 / §20.5 describe a service map, pipeline DAG, and
ML lifecycle objects with visual failure states and explained transitions.

## Decision

1. Add three offline simulators under `android-app/.../simulation/`:
   - `servicemap` — topology nodes/edges with health and failure status
   - `pipeline` — DAG stages with run status, retries, and quality gates
   - `mlops` — model/dataset/deployment lifecycle objects
2. Player interaction is **controlled actions** (not freeform graph editing).
   Each action has `requires` / `effects` maps, matching the systems-panel pattern.
3. Every successful action writes a human-readable `lastExplanation` so state
   changes are explained to the player.
4. Objective types `service_map_state`, `pipeline_state`, and `mlops_state`
   assert node/artifact fields via string maps (same `assert` shape as other tools).
5. Simulations are seeded from mission YAML and reset to that seed; no network.

## Alternatives considered

- Full infrastructure sandbox — rejected; out of scope and non-deterministic.
- Freeform node editor — deferred; controlled actions teach the concept faster on mobile.

## Consequences

- Content authors define graphs + actions in `environment.service_map|pipeline|mlops`.
- Missions can combine these tools with existing simulators later.

## References

- `docs/PRODUCT_SPEC.md` §9.7, §20.4, §20.5, §32 Epic 10
