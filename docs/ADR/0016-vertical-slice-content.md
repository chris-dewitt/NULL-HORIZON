# ADR-0016: Vertical-slice mission set and offline fallback for online missions

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 11

## Context

Epic 11 requires twelve polished missions in a playable sequence, including at
least one online-execution mission with an offline explanatory fallback, plus
recorded playtest findings.

## Decision

1. Add a `vertical_slice` chapter whose `mission_ids` list is the ordered
   twelve-mission slice from PRODUCT_SPEC §32 Epic 11 / §33.
2. Reuse existing polished missions where they already match the slice intent;
   author new missions for gaps (wake, process stop, git recover, regression
   test, API trace, unsafe query, integrated incident).
3. Extend the terminal simulator with a seeded process table (`ps` / `kill`) and
   `process_state` objectives.
4. Extend Git `checkout <commit>` to restore a historical tree for recovery
   missions.
5. For the online mission (`automation.regression_guard.01`):
   - Set `requirements.online: true`.
   - Keep deterministic `fake` fixtures as the offline evaluation path.
   - Author `narrative.offline_fallback_dialogue_id` explaining that remote
     execution is unavailable and local fixtures are used instead.
6. Record playtest findings under `docs/playtests/`.

## Alternatives considered

- Require live remote runner for the online mission in CI — rejected; CI stays
  hermetic with fake fixtures while documenting the online contract.
- Replace all prior chapter missions — rejected; domain chapters remain for
  Epic 12 curriculum growth.

## Consequences

- Ship map / mission list can present the slice via the `vertical_slice` chapter.
- Process and commit-checkout support become reusable for later curriculum.

## References

- `docs/PRODUCT_SPEC.md` §32 Epic 11, §33, §34
