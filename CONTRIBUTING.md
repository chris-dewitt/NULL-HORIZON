# Contributing

Thank you for helping build NULL HORIZON.

## Before you start

1. Read [AGENTS.md](AGENTS.md) and [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md).
2. Check existing ADRs in [docs/ADR/](docs/ADR/).
3. Prefer small pull requests that map to a single concern.

## Development workflow

1. Create a focused branch from `main`.
2. Implement the change with tests for meaningful behavior.
3. Run `./scripts/check.sh`.
4. Open a pull request using the repository template.
5. Do not push directly to `main`.

## Architecture changes

If a change conflicts with the product specification or an existing ADR:

1. Propose a new ADR using `docs/ADR/0000-template.md`.
2. Explain the decision, alternatives, and consequences.
3. Do not land conflicting architecture without that record.

## Content changes

Mission content lives under `content/` and must remain data-driven. Do not hardcode mission-specific logic into generic UI or API handlers.

## Security expectations

- Never commit secrets, credentials, or private keys.
- Never execute learner-authored code in the API process.
- Do not weaken runner isolation without an approved ADR and security tests.
- Do not use an LLM for deterministic mission evaluation.

## Code of conduct

Participation is governed by [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
