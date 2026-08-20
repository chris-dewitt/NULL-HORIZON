# Google Play Data safety form (v1)

Aligned with ADR-0024 offline-first posture and `docs/legal/privacy-policy.md`.

## Overview answers

| Question | Answer |
| --- | --- |
| Does the app collect or share user data? | **No** for the default local build (no analytics, no crash upload, no cloud sync, no remote execution). |
| Is all user data encrypted in transit? | N/A when nothing is transmitted. If you later enable sync/execution, require TLS and update this form. |
| Can users request deletion? | **Yes** — Settings → Delete local data. |

## Data types (default v1)

| Type | Collected? | Shared? | Notes |
| --- | --- | --- | --- |
| Name / callsign | Stored **on device only** | No | Operator callsign; not verified identity |
| App activity / analytics | No (default off; no collector shipped) | No | Toggle reserved for a future collector |
| Crash logs | No (local stub only) | No | No vendor SDK in v1 |
| Source code / messages | On device in mission UI state | No | Not transmitted in this build |
| Device IDs | No | No | |

## Security practices

- Data is encrypted in transit: **Not applicable** (no network collection in v1).
- Users can request that data be deleted: **Yes**.
- Committed to follow Play Families policies if targeting children: v1 targets beginners learning backend skills; do not declare “Designed for Families” unless you complete that program separately.

## Future changes that require form updates

- Enabling cloud sync
- Enabling online execution
- Enabling a crash or analytics SDK
