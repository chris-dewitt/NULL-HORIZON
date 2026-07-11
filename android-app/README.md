# NULL HORIZON Android app

Jetpack Compose client. Epic 1 delivers the application shell: navigation, design tokens,
local profile, ship-map placeholder, mission list placeholder, and accessibility settings.

## Build

```bash
cd android-app
./gradlew test
./gradlew lintDebug
./gradlew assembleDebug
```

Requires JDK 17+ and an Android SDK with platform 35 installed. Set `ANDROID_HOME` or create `local.properties` with `sdk.dir`.

## Epic 1 / Epic 2 surfaces

- Local profile setup (no account)
- Bottom navigation: Ship Map, Missions, Settings
- Accessibility toggles: high contrast, reduced motion, larger text
- Data-driven missions loaded from `assets/content` (Emergency Lighting)

Rebuild content into assets after YAML changes:

```bash
python scripts/build_bundle.py --channel dev --sync-android-assets
```

Simulators and additional domains arrive in later epics.
