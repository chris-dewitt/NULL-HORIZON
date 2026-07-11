# NULL HORIZON Android app

Minimal Jetpack Compose shell for Epic 0.

## Build

```bash
cd android-app
./gradlew test
./gradlew assembleDebug
```

Requires JDK 17+ and an Android SDK with platform 35 installed. Set `ANDROID_HOME` or create `local.properties` with `sdk.dir`.

## Scope

Epic 0 includes a boot screen only. Navigation, design tokens, ship map, and settings arrive in Epic 1.
