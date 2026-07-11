# Crash reporting privacy review

- Date: 2026-07-11
- Decision: ADR-0018

## Requirements

- Opt-in only (default off)
- No raw learner source
- No terminal history payloads
- No SQL text
- No secrets/tokens
- No automatic network transport until a vendor SDK ADR is approved

## Implementation

`LocalNoOpCrashReporter` enforces blocked metadata keys and never transmits off-device.

## Verdict

**Pass** for closed testing with local stub. Do not enable a third-party crash SDK without a new ADR and human approval.
