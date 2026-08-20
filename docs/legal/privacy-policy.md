# NULL HORIZON Privacy Policy

Effective date: 2026-08-19  
Status: Ready to host for Google Play (see `docs/store/privacy-policy.html` and ADR-0024)

## Summary

NULL HORIZON defaults to **local play**. You do not need an email or real name to start the opening campaign. Progress is stored on your device unless a future version lets you opt into cloud sync.

## Data we may store on device

- Operator callsign (local profile display name)
- Mission progress, skill evidence, rewards, and rank
- Accessibility and privacy preferences

## Data we do not collect by default

- Analytics events (off unless you enable them; no collector ships in v1)
- Crash reports (off unless you enable them; v1 uses a local stub that does not upload)
- Raw learner source code off-device (this Play build does not enable remote execution)
- Terminal history, SQL text, or secrets in telemetry

## Online code execution

This Play release does not transmit mission source to a remote execution service. Missions use on-device simulators and guided exercises. If a later version enables optional online execution, that version’s store listing and this policy will describe what is sent, retention, and consent. Learner code is never executed inside the API process.

## Cloud sync (optional, future)

Cloud sync is not enabled in the v1 Android client UI. Backend deletion endpoints (`DELETE /v1/progress`, `DELETE /v1/profiles/me`) exist for a future sync client.

## Your controls

In Settings you can:

- Export local player data as JSON
- Delete local profile, progress, and privacy settings
- Enable/disable analytics and crash reporting preferences (both default off)

## Sale of data

We do not sell personal data.

## Contact

Security and privacy reports: see `SECURITY.md`. Add the Play Console support email when the listing is created.
