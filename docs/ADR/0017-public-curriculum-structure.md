# ADR-0017: Public-version curriculum structure (Epic 12)

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 12

## Context

Epic 12 requires the complete initial public campaign from PRODUCT_SPEC §11:
approximately seventy-two core missions across fourteen chapters, with every
domain offering introduction, practice, and capstone work, a valid skill
prerequisite graph, and a defensive-security review for Black Vault content.

## Decision

1. Author campaign chapters with stable ids matching §11 regions:
   `emergency_interface`, `maintenance_deck`, `version_vault`, `archive_core`,
   `automation_lab`, `drone_foundry`, `navigation_array`, `communications_spire`,
   `verification_chamber`, `black_vault`, `data_foundry`, `reactor_kernel`,
   `prediction_observatory`, `horizon_core`.
2. Keep `vertical_slice` as an ordered onboarding path; it is not a domain chapter.
3. Map mission `difficulty` to curriculum role:
   - `introductory` → introduction
   - `practiced` → practice
   - `challenge` → capstone (or late practice when a chapter has multiple challenges)
4. Every domain chapter must include at least one mission of each difficulty.
5. Prefer existing simulators (terminal, git, sql, python fake fixtures, service
   map, pipeline, mlops, systems panel). Do not add real attack tooling.
6. Black Vault missions stay defensive and fictional; record the review in
   `docs/security/black-vault-defensive-review.md`.
7. Extend content validation to enforce domain intro/practice/capstone coverage
   and acyclic skill prerequisites.

## Alternatives considered

- Defer C++ / DSA chapters until new editors ship — rejected; fake fixtures and
  systems-panel missions teach the concepts within current tools.
- Require live runners for curriculum CI — rejected; hermetic fake fixtures remain
  the default evaluation path.

## Consequences

- Bundle version advances with the full campaign.
- Domain chapters supersede interim chapter naming from earlier epics
  (`comms_relay` → `communications_spire`, `incident_bridge` → `horizon_core`).

## References

- `docs/PRODUCT_SPEC.md` §11, §32 Epic 12, §9.8 (defensive security)
