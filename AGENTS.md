# AGENTS.md

Operating rules for human and agentic contributors working in this monorepo.

## Before changing code

1. Read this file, `docs/PRODUCT_SPEC.md`, relevant ADRs under `docs/ADR/`, and nearby tests.
2. Do not invent a different architecture without proposing an ADR.
3. Prefer the simplest implementation that preserves the specified interfaces.
4. Keep learning content separate from platform code.
5. Do not hardcode mission-specific logic into generic UI.

## Security

1. Never commit secrets or credentials.
2. Never print tokens or private keys.
3. Never mount the Docker socket into a runner.
4. Never execute learner-authored code in the API process.
5. Never grant privileged container access for learner execution.
6. Never enable network for learner code without an approved ADR.
7. Never use production credentials in tests.
8. Never publish to stores or production without explicit human approval.
9. Flag any request that weakens isolation.
10. Add security tests with runner changes.

## Quality

1. Work in small, reviewable increments.
2. Add tests for meaningful behavior.
3. Do not silently weaken tests.
4. Do not replace deterministic simulation or mission evaluation with an LLM.
5. Do not add dependencies without documenting why.
6. Before marking a task complete, run `./scripts/check.sh` (or the closest available equivalent).

## Git

1. Do not push directly to the protected `main` branch.
2. Use focused branches.
3. Make coherent commits.
4. Do not rewrite shared history.
5. Do not remove unrelated work.
6. Do not combine whole-repository formatting with a feature change.
7. Include migrations and generated schemas when required.
8. Keep generated artifacts clearly identified.

## Epic boundaries

- Epic 0 establishes repository standards, documentation, CI skeleton, and minimal compilable Android and FastAPI shells.
- Epic 1 delivers the Android application shell: navigation, design tokens, local profile, ship-map and mission placeholders, and accessibility settings.
- Epic 2 delivers the mission content engine: schemas, YAML→JSON bundles, validation CLI, local content repository, and deterministic mission/objective/hint engines.
- Epic 3 delivers the simulated terminal: virtual filesystem, command parser/registry, terminal UI, and command/filesystem objective assertions.
- Epic 4 delivers the simulated Git repository: status/diff/add/commit/log/branch/switch/merge, conflict UI, and git_state objectives.
- Epic 5 delivers the SQL console: isolated mission SQLite, select-only policy, schema browser, result table, and sql_result/database assertions.
- Do not begin the production execution sandbox, production infrastructure, or full mission curriculum until their epics begin.
- Learner-authored code must remain completely separated from the API process at every stage.

## Completion report

When finishing a task, report:

- Files changed
- Tests run
- Tests not run and why
- Security implications
- Follow-up work
- Any deviation from the specification
