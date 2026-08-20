# Accessibility audit (manual)

- Date: 2026-07-13
- Scope: Epic 1 shell + Settings privacy/data controls + mission panels + terminal-console redesign (ADR-0021)

## Checks

| Area | Result | Notes |
| --- | --- | --- |
| Content descriptions on primary nav | Pass | Top-level destinations labeled |
| Settings toggles | Pass | Switch semantics present |
| Export/delete actions | Pass | Explicit content descriptions |
| Touch target guidance | Partial | Material3 defaults; revisit dense mission panels and PC TUI density on Android back-port |
| Reduced motion / larger text / high contrast prefs | Pass | Stored and applied via settings; boot/typewriter/cursor gated by reduced motion |
| Disable CRT pref | Pass (policy) | Independent toggle; also forced off under high contrast |
| Color-only status | Partial | Region accents require textual status (`DESIGN_SYSTEM.md`); revisit mission panels |
| CRT overlays | Pass (policy) | Medium on PC, Lean on Android; gated by Disable CRT + high contrast |
| Larger text + TUI panels | Partial | PC density wired; verify no clipped CTAs after full screen migration |

## Verdict

Accessibility basics pass for closed testing and the offline-first Play v1
posture (ADR-0024). Dense simulator panels and Android TUI density remain a
follow-up polish item, not a store-upload blocker.

