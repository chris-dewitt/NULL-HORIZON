# Closed test track process

1. Build a signed release or debug-internal APK/AAB from `android-app`.
2. Upload to Google Play **closed testing** track only (human approval required).
3. Attach privacy policy URL and listing copy from `docs/store/`.
4. Invite internal testers; require airplane-mode smoke of vertical slice.
5. Collect crash/ANR only if testers opt in to crash reporting.
6. Blockers: critical security findings, data-deletion failures, missing online-execution disclosure.
7. Do **not** promote to production from this epic.
