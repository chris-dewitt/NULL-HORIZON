# NULL HORIZON Privacy Policy

Effective date: 2026-07-11  
Status: Draft for closed testing (not a store-published legal final)

## Summary

NULL HORIZON defaults to **local play**. You do not need an email or real name to start the opening campaign. Progress is stored on your device unless you later opt into cloud sync.

## Data we may store on device

- Operator callsign (local profile display name)
- Mission progress, skill evidence, rewards, and rank
- Accessibility and privacy preferences

## Data we do not collect by default

- Analytics events (off unless you enable them)
- Crash reports (off unless you enable them)
- Raw learner source code, except when you explicitly run an **online** mission execution
- Terminal history, SQL text, or secrets in telemetry

## Online code execution

Some missions may offer online execution. When you run those missions online:

- Source required for that run may be transmitted to the execution service
- Results (tests, diagnostics, coarse metrics) may be returned to the app
- Retention is short by default; learner code is not used for model training without separate explicit consent
- Learner code is never executed inside the API process

Offline fallbacks exist for online-marked missions when remote execution is unavailable.

## Cloud sync (optional)

If you create/use an anonymous cloud profile:

- Account identifier (opaque token)
- Progress, rewards, skill evidence
- Device sync metadata

You can delete cloud progress (`DELETE /v1/progress`) or the entire cloud profile (`DELETE /v1/profiles/me`).

## Your controls

In Settings you can:

- Export local player data as JSON
- Delete local profile, progress, and privacy settings
- Enable/disable analytics and crash reporting (both default off)

## Sale of data

We do not sell personal data.

## Contact

Security and privacy reports: see `SECURITY.md`.
