# NULL HORIZON

Narrative-driven Android game that teaches backend software development by making code the player's primary tool for surviving aboard a damaged interstellar colony ship.

> Working title only. Not cleared for trademark, store-name, or domain availability.

## Status

Epics 0–13 are implemented in-repo: playable curriculum through the public campaign, plus release-readiness privacy/deletion controls and ops docs. Store publish and production sandbox enablement still require explicit human approval.

## Repository layout

```text
android-app/   Kotlin + Jetpack Compose client
backend/       FastAPI API, runner contracts, shared schemas
content/       Versioned mission and curriculum data
shared/        OpenAPI, JSON Schema, fixtures
infra/         Local Compose, Terraform placeholders, policies, images
scripts/       Bootstrap and validation helpers
docs/          Product spec, architecture, ADRs, authoring guides
```

## Prerequisites

- JDK 17+ (Android Gradle Plugin compatibility)
- Android SDK / Android Studio for client work
- Python 3.12+
- Docker and Docker Compose for trusted local backend development (later epics)
- Git

Exact versions are pinned as tooling lands. See `docs/PRODUCT_SPEC.md` §30.

## Quick start

```bash
./scripts/bootstrap.sh
./scripts/check.sh
```

### Backend only

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
pytest
uvicorn api.app.main:app --reload --app-dir .
```

### Android only

```bash
cd android-app
./gradlew test
./gradlew assembleDebug
```

## Documentation

| Document | Purpose |
|---|---|
| [AGENTS.md](AGENTS.md) | Contributor and coding-agent operating rules |
| [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md) | Authoritative product and engineering specification |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture overview |
| [docs/THREAT_MODEL.md](docs/THREAT_MODEL.md) | Security threat model skeleton |
| [docs/API.md](docs/API.md) | API notes |
| [docs/legal/privacy-policy.md](docs/legal/privacy-policy.md) | Privacy policy ([summary](PRIVACY.md)) |
| [docs/store/listing.md](docs/store/listing.md) | Store listing draft and online-execution disclosure |
| [docs/CONTENT_AUTHORING.md](docs/CONTENT_AUTHORING.md) | Mission authoring workflow |
| [docs/CURRICULUM.md](docs/CURRICULUM.md) | Curriculum map |
| [docs/ADR/](docs/ADR/) | Architecture decision records |
| [SECURITY.md](SECURITY.md) | Vulnerability reporting |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guide |

## License

MIT — see [LICENSE](LICENSE). Final open-source licensing posture is tracked in ADR-0015.

## Security note

Learner-authored code is hostile input. It must never run inside the API process. Deterministic mission evaluation must not use an LLM.
