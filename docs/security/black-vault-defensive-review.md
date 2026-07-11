# Black Vault defensive-security review

- Date: 2026-07-11
- Scope: Epic 12 `black_vault` chapter missions
- Reviewer: agent curriculum pass against PRODUCT_SPEC §9.8 / Epic 12 acceptance

## Missions reviewed

| Mission | Defensive focus | Offensive content |
| --- | --- | --- |
| `security.threat_model.01` | Asset / actor / entry-point modeling | None |
| `security.validation.01` | Input length validation on fictional operator text | None |
| `archive.unsafe_query.01` | Replace string-built SQL with placeholders | No exploit payload; teaches prevention |
| `security.authz.01` | Separate authentication from authorization | None |
| `security.contain_capstone.01` | Least-privilege containment of a fictional service account | No disable-life-support path required |

## Findings

- All Black Vault missions use fictional ship systems and controlled actions or local fake fixtures.
- No mission provides deployable instructions for attacking real services, networks, or credentials.
- SQL injection content is limited to repairing unsafe string concatenation in a sandbox helper.
- Secret-handling guidance uses fictional `vault://` references only; no real tokens appear in content.
- Capstone containment keeps life support online by assertion.

## Verdict

**Pass** for defensive-security curriculum review. No blocking issues for Epic 12 acceptance.
