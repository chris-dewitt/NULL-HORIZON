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

## Epic 1 surfaces

- Local profile setup (no account)
- Bottom navigation: Ship Map, Missions, Settings
- Accessibility toggles: high contrast, reduced motion, larger text
- Placeholder mission list for Emergency Interface

Mission content loading and simulators arrive in later epics.
