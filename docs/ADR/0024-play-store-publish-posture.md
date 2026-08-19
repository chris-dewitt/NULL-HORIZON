# ADR-0024: Google Play store publish posture (v1)

- Status: Accepted
- Date: 2026-08-19
- Deciders: Product owner human approval (cloud agent request)

## Context

Epic 13 prepared closed-testing release readiness. Human approval was granted
to proceed toward Google Play publication. Several PRODUCT_SPEC §41 open
questions remain product decisions that must be recorded before upload.

## Decision

1. **Store target:** Google Play. First public track may be production or
   open testing after closed testing; agents still cannot operate Play Console
   credentials — the human publisher uploads the AAB.
2. **Title:** Ship as **NULL HORIZON**. Trademark / store-name clearance risk
   is accepted by the publisher; rename later if a conflict appears.
3. **License:** Keep **MIT** (`LICENSE`, ADR-0015) for v1.
4. **minSdk:** Confirm **26** (Android 8.0) per ADR-0014 for v1.
5. **Accounts / auth:** Local-only play for v1. No account required. Cloud
   sync UI remains unwired; do not market cloud accounts as available.
6. **Online execution / sandbox:** Keep `HardenedSandboxProvider` **blocked**
   (ADR-0011). V1 ships offline-first with local simulators and fake/offline
   execution fixtures. Do not enable public remote execution without a later
   ADR and security review.
7. **Monetization:** Free core; no ads and no paid campaigns in v1.
8. **Crash / analytics:** Remain **off by default**. Crash reporting stays a
   local no-op stub until a vendor SDK ADR. Do not claim live crash reporting
   in the store listing.
9. **Privacy policy:** Treat `docs/legal/privacy-policy.md` (and the HTML
   mirror under `docs/store/`) as the policy text to host at a public URL
   before Play review.
10. **Signing:** Use a publisher-held upload keystore via
    `android-app/keystore.properties` (never committed). Play App Signing
    recommended in Play Console.

## Alternatives considered

- Delay publish until trademark clearance and hardened sandbox — rejected by
  explicit human approval to publish now with offline-first posture.
- Enable public sandbox for “complete” online Python/C++ — rejected; weakens
  isolation and violates ADR-0011.

## Consequences

- Store listing must disclose offline-first behavior and that advanced online
  execution is not enabled in this build.
- Publisher must create a Play developer account, host the privacy policy URL,
  generate the upload keystore, and run `scripts/build_play_bundle.sh`.
- Follow-up ADRs still required for: trademark rename, auth provider, cloud
  provider, hardened sandbox enablement, crash SDK vendor.

## References

- `docs/release/PLAY_PUBLISH.md`
- `docs/store/listing.md`
- `docs/ADR/0011-trusted-runner-vs-blocked-public-sandbox.md`
- `docs/PRODUCT_SPEC.md` §31.3, §41
