# Content Authoring

Mission content is data under `content/`. Authoring workflow details are in [PRODUCT_SPEC.md](PRODUCT_SPEC.md) §21 and §28.

## Principles

- Write missions as YAML, compile to validated JSON bundles.
- Keep one primary learning objective per mission.
- Provide progressive hints and deterministic completion assertions.
- Never leak hidden-test answers into client-visible content.
- Cybersecurity missions remain defensive and fictional.

## Layout

```text
content/
├── schema/
├── skills/
├── chapters/
├── missions/
├── environments/
├── dialogue/
└── tools/
```

## Validation

Epic 0 includes placeholder directories only. Schema validation and bundle build tooling arrive in Epic 2.
