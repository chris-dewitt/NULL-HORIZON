# Google Play publish runbook (v1)

Human approval for store publish is recorded in ADR-0024. This document is the
operator checklist for uploading NULL HORIZON. Agents cannot complete Play
Console steps that require your Google account.

## What this release includes

- Offline-first Android campaign (local profile, no account required)
- Simulated terminal / Git / SQL / editor missions from the content bundle
- Local export and delete in Settings
- Analytics and crash reporting **off by default** (crash reporter is a local stub)

## What this release does **not** include

- Public hardened remote code execution (still blocked — ADR-0011)
- Cloud sync UI
- Third-party crash SDK
- Ads or IAP

## Prerequisites (you must do these)

1. **Google Play Console** developer account (one-time registration fee).
2. Create app **NULL HORIZON**, package `com.nullhorizon.app`.
3. **Host the privacy policy** at a public HTTPS URL.
   - Source: `docs/legal/privacy-policy.md`
   - HTML mirror ready to host: `docs/store/privacy-policy.html`
   - Example: GitHub Pages, Cloudflare Pages, or your domain.
4. Create an **upload keystore** (once) and keep backups offline:

```bash
mkdir -p android-app/keystore
keytool -genkeypair -v \
  -keystore android-app/keystore/nullhorizon-upload.jks \
  -alias nullhorizon \
  -keyalg RSA -keysize 2048 -validity 10000
cp android-app/keystore.properties.example android-app/keystore.properties
# edit passwords — never commit keystore.properties or *.jks
```

5. Enable **Play App Signing** when Play Console prompts (recommended).

## Build the AAB

```bash
export PLAY_VERSION_NAME=1.0.0
export PLAY_VERSION_CODE=1   # increment for every Play upload
./scripts/build_play_bundle.sh
```

Output: `dist/play/nullhorizon-1.0.0-1.aab`

## Play Console listing

Use copy from `docs/store/listing.md`.

Upload assets from `docs/store/assets/`:

| Asset | File |
| --- | --- |
| High-res icon 512×512 | `icon-512.png` |
| Feature graphic 1024×500 | `feature-graphic-1024x500.png` |
| Phone screenshots | `screenshot-01-boot.png` … `screenshot-06-settings.png` |

Replace stylized screenshots with device captures before promoting widely if you want photoreal UI.

### Data safety

Fill the form using `docs/store/data-safety.md`.

### Content rating

Complete the IARC questionnaire using `docs/release/content-rating.md`.

### Categories / tags

- Category: **Education** (primary) or **Puzzle** / simulation if Education is unavailable
- Tags: learning, coding, programming, puzzle (as allowed)

## Recommended track sequence

1. Upload AAB to **Internal testing** → smoke install.
2. Promote to **Closed testing** → invite testers; airplane-mode vertical slice.
3. Review **Pre-launch report**.
4. Promote to **Production** (or Open testing first) when you are satisfied.

Rollback: `docs/ops/rollback.md`.

## Store review tips

- Privacy policy URL must match in-app disclosures (Settings privacy summary).
- Declare that learner code stays on-device for this build; online remote execution is not enabled.
- Do not claim real-world hacking tools; Black Vault is defensive curriculum only.

## After publish

- Tag the git commit used for the AAB (`v1.0.0`).
- Store the upload keystore and passwords in a password manager.
- File follow-up issues for: trademark clearance, crash SDK ADR, cloud sync, hardened sandbox.
