# ADR-0021: MU-TH-UR / terminal-console visual language

- Status: Accepted
- Date: 2026-07-13
- Deciders: Product + client design direction (PC-first redesign)

## Context

Section 15 of `PRODUCT_SPEC.md` described “functional retro-future” as dark
graphite panels, warm off-white text, muted brass accents, and optional CRT
hints. The shipped Compose shell followed that literally: Material3 dark
schemes, rounded panel borders, and a single brass primary.

Playtest and design review want a denser **MU-TH-UR / TUX console** look:
phosphor text on black, per-region accents, box-drawing panel chrome, CRT
overlays, boot/typewriter motion, and ALL-CAPS system labels — while keeping
high-contrast, larger-text, and reduced-motion accessibility intact.

PC (`pc-app/`) is the design canvas; Android stays leaner but shares tokens and
primitives via `shared/client-core` (ADR-0020).

## Decision

1. **Replace the graphite+brass default palette** with a CRT terminal palette:
   near-black backgrounds and phosphor text (white / green / amber / red / blue).
   Material3 schemes remain the Compose wiring layer; semantic tokens live in
   `NhColors` / `NhRegionAccent`.
2. **Per-ship-region accent colors** are first-class design tokens keyed by
   stable region ids (`emergency`, `archive`, `black_vault`, …). Status and
   region identity must still include text labels (never color alone).
3. **Panel chrome is TUI-style**, not Material cards: shared `TuiPanel` draws
   box-drawing borders with an ALL-CAPS title inset (`┌─ SYSTEMS ─┐`).
4. **CRT presentation** (scanlines, vignette, optional flicker/bloom) is a
   shared overlay. It is **off** when reduced-motion or high-contrast
   accessibility prefs are enabled; high-contrast also flattens to
   white-on-black without glow.
5. **Motion moments** (boot sequence, typewriter dialogue, blinking block
   cursor) are shared primitives. Instant reveal when reduced motion is on.
6. **Platform split:** PC may use wider layouts and keybind hints; Android
   reuses tokens/primitives but keeps denser chrome optional and touch-first.
7. Open product/visual questions that are not yet locked live in
   `docs/DESIGN_SYSTEM.md` and must not block the token/primitives landing.

## Alternatives considered

- **Keep graphite+brass, add CRT as a cosmetic theme unlock** — rejected as the
  default product identity; cosmetics may still layer later (§13 rewards).
- **Full custom non-Material UI toolkit** — deferred; Material3 remains the
  accessibility/focus substrate while chrome is restyled.
- **Android-first redesign** — rejected for iteration speed; PC is the
  screenshot/react canvas, then back-port.

## Consequences

- Spec §15 and accessibility audit notes must track the new language.
- Shared theme changes affect both clients immediately; screen migrations can
  land incrementally (PC ship map / shell first).
- Glow/bloom and flicker must stay subtle and gated by accessibility prefs.
- Follow-up: migrate mission panels, dialogue, and Android shell; decide
  remaining open questions in `docs/DESIGN_SYSTEM.md`.

## References

- `docs/PRODUCT_SPEC.md` §7 (regions), §14 (accessibility), §15 (visual)
- `docs/DESIGN_SYSTEM.md`
- ADR-0019 Compose Desktop PC client
- ADR-0020 Shared client-core source set
