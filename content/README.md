# Content

Versioned mission and curriculum data. Author in YAML, compile to JSON bundles.

## Layout

- `schema/` — JSON Schema contracts
- `skills/`, `chapters/`, `missions/`, `dialogue/`, `rewards/` — authored YAML
- `build/bundles/` — generated JSON bundles (dev channel)

## Commands

```bash
pip install -r scripts/requirements-content.txt
python scripts/validate_content.py
python scripts/build_bundle.py --channel dev --sync-android-assets
pytest scripts/tests
```

Invalid content must fail validation. Do not hardcode mission-specific logic into the Android UI.
