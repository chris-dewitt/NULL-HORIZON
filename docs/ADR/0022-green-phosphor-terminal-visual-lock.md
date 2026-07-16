# ADR-0022: Green phosphor terminal visual lock

- Status: Accepted
- Date: 2026-07-14
- Deciders: Product request for NULL HORIZON visual overhaul
- Supersedes: ADR-0021 decisions about CRT curvature, bloom, and idle flicker

## Context

ADR-0021 moved NULL HORIZON away from Material-style panels into a dense
terminal-console language. The next visual lock narrows that language further:
the target is a green phosphor terminal with tmux-like pane structure,
box-drawing chrome, status glyphs, and a restrained 1970s-console voice.

The product must remain readable for intimidated beginners. Long-form briefing,
lesson, SQL, and code text cannot depend on glow, flicker, or distorted glass
effects.

## Decision

1. **Palette is green phosphor first.** The base app ground is near-black dark
   green. Primary system text and accents are green; amber remains the selected
   tab / warning accent; blue and red remain informational and critical accents.
2. **CRT effects are scanlines plus vignette only.** Do not add text glow,
   geometric screen curvature, bloom, or global flicker.
3. **Pane chrome remains shared.** `TuiPanel`, `TuiActionButton`,
   `TuiNavColumn`, `TuiTabLine`, `TerminalPromptField`, and related primitives
   under `shared/client-core/.../ui` are the source of truth for both Android
   and PC.
4. **Navigation uses tmux semantics.** Android uses `TuiTabLine`; PC keeps
   `TuiNavColumn` but selected rows invert to amber/black to match the tab-line
   visual language.
5. **Motion is limited and gated.** Boot sequence, ORION/MICA typewriter
   reveal, block cursor blink, and damaged-status blink are the only intentional
   chrome motion. They are disabled when reduced motion or high contrast is on.
6. **High contrast disables CRT overlays and animated chrome.** The
   `crtEffectsEnabled` and `animatedChromeEnabled` gates must be used before
   introducing new visual effects.
7. **Readability wins.** Long-form body text, code, SQL, and lesson content stay
   clean and legible. Effects belong to chrome, headers, and status surfaces.

## Consequences

- `CrtProfile` no longer models curvature, bloom, or idle flicker.
- `CrtFrame` draws only scanlines and vignette when CRT effects are enabled.
- Android launch uses the same boot sequence as PC.
- Remaining Material buttons/text fields in onboarding, settings, and mission
  surfaces should migrate to shared TUI controls.

## References

- ADR-0020 Shared client-core source set
- ADR-0021 Terminal-console visual language
- `docs/DESIGN_SYSTEM.md`

