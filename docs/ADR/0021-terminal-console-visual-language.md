# ADR-0021: Terminal-console visual language (tmux × Palantir × Fallout)

- Status: Accepted
- Date: 2026-07-13
- Deciders: Product + client design direction (PC-first redesign)

## Context

Section 15 of `PRODUCT_SPEC.md` described “functional retro-future” as dark
graphite panels, warm off-white text, muted brass accents, and optional CRT
hints. The shipped Compose shell followed that literally: Material3 dark
schemes, rounded panel borders, and a single brass primary.

Design direction is a dense **operator terminal**: tmux-like pane chrome,
Palantir-style status density, Fallout-terminal phosphor readability.
MU-TH-UR / Nostromo mood is a north star only — not a costume requirement.

## Decision

1. **CRT terminal palette** (phosphor on near-black) replaces graphite+brass.
   Material3 remains the Compose wiring layer; tokens live in `NhColors` /
   `NhRegionAccent`.
2. **Per-ship-region accents** are confirmed (see `DESIGN_SYSTEM.md` §3.2).
   Status always includes text, never color alone.
3. **TUI panel chrome** via shared `TuiPanel` (box-drawing, ALL-CAPS titles).
   ALL-CAPS applies to **system chrome only**; dialogue stays sentence case.
4. **CRT presentation** defaults to **medium** intensity with **real
   geometric curvature** (barrel-style warp + bezel), plus scanlines,
   vignette, and soft bloom. PC uses `CrtProfile.Medium`; Android phones use
   `CrtProfile.Lean` (lighter, no idle flicker).
5. **Disable CRT** is a **separate accessibility preference** (`disableCrt`),
   independent of reduced motion. High contrast also forces CRT off.
   Reduced motion disables animated chrome (boot/typewriter/cursor/flicker)
   but may leave static CRT unless `disableCrt` or high contrast is on.
6. **Boot sequence** runs every cold start and is skippable.
7. **Typeface is Terminal** — VT323 (VT320 console glyphs, SIL OFL), loaded
   per platform into `NullHorizonTheme`.
8. **Platform split:** PC maximal; Android lean CRT + touch-first layout;
   shared primitives in `shared/client-core`.

## Alternatives considered

- **Alien/MU-TH-UR costume as primary identity** — rejected; keep as mood
  reference only.
- **Fold CRT off into reduced motion only** — rejected; product wants an
  explicit Disable CRT toggle.
- **Vignette-only fake curvature** — rejected; medium real warp is required.
- **Full CRT on Android phones** — deferred; lean profile first.

## Consequences

- Spec §15 and `DESIGN_SYSTEM.md` track the locked decisions.
- Settings repositories on PC and Android persist `disableCrt`.
- Follow-up: mission panel migration, dialogue typewriter wiring, font pick.

## References

- `docs/PRODUCT_SPEC.md` §7, §14, §15
- `docs/DESIGN_SYSTEM.md`
- ADR-0019, ADR-0020
