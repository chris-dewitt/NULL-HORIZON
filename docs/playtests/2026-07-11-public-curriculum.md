# Epic 12 curriculum playtest notes

- Date: 2026-07-11
- Bundle: `0.4.0`
- Method: content validation + sampled engine paths + defensive review

## Coverage

- 14 domain chapters from PRODUCT_SPEC §11 plus retained `vertical_slice` path
- 77 campaign missions (spec ~72; counts shifted with integrated extras)
- Each domain chapter has introduction/practice/capstone difficulties
  (`horizon_core` uses practiced + challenge for its two large incidents)
- Skill prerequisite graph validated acyclic (80 skills)

## Sampled paths

- Maintenance capstone process stop (`emergency.rogue_process.01`)
- Archive capstone removed-colonist count
- Black Vault defensive set (see `docs/security/black-vault-defensive-review.md`)
- Horizon Core multi-domain containment verdict

## Findings

- Generated practice missions intentionally reuse simulator templates; polish can deepen narrative voice in later content passes.
- C++ chapter teaches concepts through editable snippets + fake fixtures (no native toolchain in-app yet).
- No curriculum blocker for Epic 12 acceptance.
