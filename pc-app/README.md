# NULL HORIZON PC app

Compose Desktop client. Parallel to the Android app: same mission content bundles,
simulators, and progression rules, with keyboard-first navigation and file-backed
local storage under `~/.null-horizon`.

See [ADR-0019](../docs/ADR/0019-compose-desktop-pc-client.md).

## Build / run

```bash
cd pc-app
./gradlew test
./gradlew run
```

Requires a JDK on `PATH` / `JAVA_HOME`. **JDK 17 or 21 is recommended** for playtesting.
Kotlin 2.3 supports JDK 25, but Temurin 17/21 is the smoothest path on Windows.

Windows quick start:

```bat
cd pc-app
run.bat
```

Or:

```bat
.\gradlew.bat run
```

Optional native installers:

```bash
./gradlew packageDeb   # Linux
./gradlew packageDmg   # macOS
./gradlew packageMsi   # Windows
```

## Content

Mission JSON is loaded from classpath `resources/content/`, synced from the shared
YAML pipeline:

```bash
python scripts/build_bundle.py --channel dev --sync-pc-resources
```

## Surfaces

- Local operator profile (no account)
- Navigation rail: Ship Map, Missions, Skills, Settings
- Full mission session tools: terminal, Git, SQL, editor, service map, pipeline, ML ops
- Accessibility toggles and local data export/delete
