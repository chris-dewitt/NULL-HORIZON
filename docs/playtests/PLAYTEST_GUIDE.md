# Playtest guide (Epic 13 / opening campaign)

## Build & install

```bash
cd android-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.nullhorizon.app/.MainActivity
```

No backend is required for the opening campaign (offline missions + fake fixtures).

Optional API (cloud delete only):

```bash
cd backend && source .venv/bin/activate
uvicorn api.app.main:app --reload --app-dir .
```

## Smoke path (15–25 min)

### 1. First launch / profile
1. Confirm profile setup appears (no account required).
2. Enter a callsign (2–24 chars) → **Enter emergency interface**.
3. Confirm bottom nav: Ship Map, Missions, Skills, Settings.

### 2. Opening campaign (vertical slice first)
1. Open **Missions**.
2. Confirm the first ~12 rows are the vertical-slice path (Wake Sequence, Locate the Fault Log, … Ghost Registry).
3. Play in order (or at least these three):
   - **Wake Sequence** — systems panel actions until complete
   - **Locate the Fault Log** — `cd /var/log/life_support`, `cat fault.log`, `grep valve-3 fault.log`
   - **Pressure Threshold** — edit `THRESHOLD = 50`, run tests
4. Confirm debrief / completed status after each.

### 3. Privacy / data controls
1. **Settings** → leave Analytics / Crash reporting **off** (defaults).
2. **Export local data** → confirm JSON preview appears with callsign + progress.
3. **Delete local data** → confirm dialog → Delete.
4. Confirm you return to **profile setup** with an empty name field (no app restart).
5. Create a new callsign and re-enter the app.

### 4. Accessibility toggles
1. Toggle High contrast / Reduced motion / Larger text.
2. Confirm switches stick after leaving and returning to Settings.

## Known limitations (not blockers)
- Full curriculum (~77 missions) is listed after the vertical slice; no hard locks between chapters yet.
- Crash reporting is a local no-op stub (no vendor SDK).
- Analytics toggle stores preference only (no collector yet).
- Cloud sync UI is not wired; API deletion exists for later sync clients.

## If something fails
- Mission won’t load: rebuild assets  
  `python scripts/build_bundle.py --channel dev --sync-android-assets`
- Full suite: `./scripts/check.sh`
- Clear app data: `adb shell pm clear com.nullhorizon.app`
