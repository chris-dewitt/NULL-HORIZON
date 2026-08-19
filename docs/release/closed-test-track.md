# Closed test track process

1. Build a signed AAB with `./scripts/build_play_bundle.sh` (see `docs/release/PLAY_PUBLISH.md`).
2. Upload to Google Play **internal** then **closed testing** tracks.
3. Attach privacy policy URL (host `docs/store/privacy-policy.html`) and listing copy from `docs/store/`.
4. Invite testers; require airplane-mode smoke of the vertical slice / opening campaign.
5. Collect crash/ANR only if testers opt in; v1 stub does not upload.
6. Blockers: critical security findings, data-deletion failures, inaccurate online-execution claims.
7. Promote to production only after pre-launch report review (publisher decision; ADR-0024).
