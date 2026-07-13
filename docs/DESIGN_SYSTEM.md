# NULL HORIZON Design System

**Status:** Active redesign baseline (ADR-0021)  
**Design canvas:** PC Compose Desktop first; Android back-port  
**Reference:** MU-TH-UR / Nostromo-style console · “tricked-out TUX” terminal

This document is the living visual contract for tokens, chrome, motion, and
accessibility. Implementation primitives live under
`shared/client-core/.../ui/`.

---

## 1. Current repo state (pre-redesign)

| Area | Was | Becomes |
|---|---|---|
| Palette | Warm graphite + brass (`NhColors`) | Black CRT + phosphor text + per-region accents |
| Panels | Material surfaces / rounded borders | Box-drawing `TuiPanel` (`┌─ TITLE ─┐`) |
| CRT | Spec mentioned; not implemented | Scanlines + vignette (+ gated flicker/bloom) |
| Density | Comfortable Material padding | Tighter spacing; more info per viewport |
| Case | Title Case UI copy | ALL-CAPS labels/headers; body may stay sentence case |
| Motion | Pref flag only | Boot sequence, typewriter dialogue, block cursor |
| Platforms | Shared tokens; PC/Android screens diverge | PC maximal; Android lean; shared primitives |
| A11y | high contrast / reduced motion / larger text | Preserved and extended to CRT/motion gates |

Epic 0–13 gameplay systems remain; this redesign is presentation chrome and
does not change mission evaluation, simulators, or content schemas.

---

## 2. Visual principles

1. **Console, not dashboard.** First viewport should feel like an operator
   terminal, not a marketing site or Material gallery.
2. **Phosphor on black.** Colored text (white / green / amber / red / blue) on
   near-black; avoid purple neon, cream brochure, or glow-heavy cyberpunk.
3. **Region identity via accent + text.** Every region has an accent token
   *and* an ALL-CAPS name/status string.
4. **TUI chrome.** Panels use box-drawing borders and monospace hierarchy.
5. **Dense but readable.** Prefer tighter spacing over card stacks; larger-text
   mode must still fit primary actions without clipping critical controls.
6. **Motion is diegetic.** Boot, typewriter, and cursor sell the OS fantasy;
   reduced-motion users get instant, static equivalents.
7. **PC leads.** Wider layouts and keybind hints are OK on desktop; Android
   stays touch-first and less maximal.

---

## 3. Palette tokens

### 3.1 Base CRT

| Token | Role | Default |
|---|---|---|
| `CrtBlack` | App background | `#000000` |
| `CrtRaised` | Nested panel fill | `#0A0A0A` |
| `PhosphorWhite` | Primary readable text | `#E6E6E6` |
| `PhosphorGreen` | Nominal / OK / system | `#33FF66` |
| `PhosphorAmber` | Warning / emergency accent | `#FFB000` |
| `PhosphorRed` | Critical / Black Vault shift | `#FF3344` |
| `PhosphorBlue` | Info / cold systems | `#44AAFF` |
| `PhosphorDim` | Secondary labels | `#7A7A7A` |
| `Scanline` | Overlay stroke | `#FFFFFF` @ low alpha |

High-contrast mode: pure black background, near-white foreground, accent kept
for focus only; no scanline/glow.

### 3.2 Region accents (initial mapping)

| Region id | Display | Accent | Rationale |
|---|---|---|---|
| `emergency` | Emergency Interface | Amber | Wake / power routing |
| `maintenance` | Maintenance Deck | Green | Linux / repairs |
| `archive` | Archive Core | Green | Records / SQL |
| `vault` / `version_vault` | Version Vault | Blue | History / branches |
| `automation` | Automation Lab | Green | Python automation |
| `drone` | Drone Foundry | White | Fabrication |
| `navigation` | Navigation Array | Blue | Routing |
| `comms` | Communications Spire | Blue | APIs |
| `verification` | Verification Chamber | Amber | Tests / cert |
| `black_vault` | Black Vault | Red-shift | Containment |
| `data_foundry` | Data Foundry | Amber | Pipelines |
| `reactor` | Reactor Kernel | Red | Kernel / C++ |
| `prediction` | Prediction Observatory | Blue | ML ops |
| `horizon` | Horizon Core | White | Capstone / ORION |

Mapping is data in `NhRegionAccent`; content region strings resolve via
normalized ids.

---

## 4. Typography and density

- Font: monospace (`FontFamily.Monospace`) until a licensed CRT/terminal face
  is chosen.
- Labels / panel titles / nav: **ALL-CAPS**.
- Mission narrative body and learner-facing errors: sentence case for
  readability (open question: shout dialogue too?).
- Default density: reduced padding vs Material defaults; larger-text multiplies
  `fontScale` (~1.15×) without changing box-drawing geometry rules.
- Status lines prefer `REGION: ARCHIVE CORE — DEGRADED` patterns.

---

## 5. Components

| Primitive | Purpose | A11y notes |
|---|---|---|
| `TuiPanel` | Box-drawing bordered section | Title is text; not color-only |
| `CrtFrame` | Scanlines + vignette wrapper | Disabled for reduced motion / high contrast |
| `TypewriterText` | ORION/MICA dialogue reveal | Instant if reduced motion |
| `BlockCursor` | Blinking input caret | Static block if reduced motion |
| `BootSequence` | Launch OS check theatre | Skippable; instant if reduced motion |
| `RegionStatusLine` | ALL-CAPS region + status | Accent + textual status |

PC-only affordances (allowed to diverge): keybind hint rows, multi-column
shell, wider mission split panes.

---

## 6. Motion moments

1. **Boot (app launch):** `NULL HORIZON OS v0.1` → memory/systems checks → OK.
2. **Dialogue:** typewriter reveal for ORION/MICA lines.
3. **Input:** blinking block cursor on terminal/editor command fields.

Flicker/bloom: rare, subtle, never required to read content; hard-off under
reduced motion and high contrast.

---

## 7. Accessibility contract (non-negotiable)

| Pref | Effect |
|---|---|
| High contrast | White-on-black; no glow/scanlines; accents only for non-essential chrome |
| Reduced motion | No boot animation, typewriter, cursor blink, or flicker |
| Larger text | `fontScale` boost; panels reflow; no clipped primary CTAs |
| Color-blind / color-only | Status always includes text or symbol, not hue alone |
| Screen readers | Panel titles and region status remain in semantics |

Touch targets on Android still follow platform guidance even when density
increases on PC.

---

## 8. Migration plan

1. **Docs + tokens + shared primitives** (this change).
2. **PC shell:** boot sequence, CRT frame, nav labels, ship map `TuiPanel`.
3. **PC mission session / dialogue / terminal input cursor.**
4. **Android shell back-port** (leaner CRT; same tokens).
5. **Remaining screens** (skills, settings, debrief) without Material cards.

Do not rewrite mission engines or content YAML for cosmetics.

---

## 9. Open questions (need answers)

Please answer as many as you can; defaults in parentheses are what this first
push assumes.

### Identity and tone
1. Is “MU-TH-UR console / tricked-out TUX” the **permanent** brand look, or a
   launch skin with unlockable themes later? *(permanent default; cosmetics later)*
2. How hard should we lean into Alien/Nostromo homage vs original NULL HORIZON
   IP? Any legal/style limits on glyph shapes, fonts, or boot copy?
3. Should ORION/MICA speak in ALL-CAPS always, or only system chrome?

### Palette
4. Confirm region accent table in §3.2 — any swaps (e.g. Archive green vs blue)?
5. One global phosphor green for “OK”, or always use the active region accent
   for success? *(global OK green; region accent for chrome)*
6. Black Vault “red-shift”: pure red, magenta-red, or dim red + white text?

### CRT effects
7. Default CRT intensity: subtle / medium / heavy? *(subtle)*
8. Screen curvature: real geometric warp, or vignette-only fake? *(vignette-only first)*
9. Flicker frequency acceptable, or boot-only? *(boot + rare idle; never in missions if distracting)*
10. Should high contrast disable CRT entirely or keep faint scanlines?

### Chrome and layout
11. Replace Material `NavigationRail` with a pure TUI side column on PC?
12. Mission session: keep multi-panel Material borders until `TuiPanel` lands
    everywhere, or big-bang replace?
13. Box-drawing: Unicode characters in Text, or Canvas-stroked lines with a
    title cutout? *(Canvas-stroked for layout stability)*
14. PC keybind hints: always visible, or Settings toggle / first-run only?

### Density and copy
15. Target information density relative to current Material screens
    (~25% tighter / 50% / terminal-max)?
16. Which strings stay Title Case for store/accessibility clarity
    (app name, settings legal)?
17. Larger text: 1.15× enough, or expose 1.0 / 1.15 / 1.3?

### Motion
18. Boot sequence: every cold start, once per day, or once ever with skip?
    *(every cold start; click/key skips)*
19. Typewriter speed (cps) and punctuation pauses?
20. Should boot be skippable on Android always for short sessions?

### Platform split
21. Which PC-only maximal features are must-have in v1 of the redesign
    (keybinds, multi-column ship map, CRT bezel chrome)?
22. Android: CRT overlay at all on phones, or tokens-only until tablets?

### Accessibility / QA
23. Do we need a fourth pref “Disable CRT effects” separate from reduced
    motion? *(no for now; fold into reduced motion + high contrast)*
24. Any seizure-safety hard caps beyond reduced motion (no strobing > X Hz)?
25. Should screenshots for store listing use CRT on or off?

### Engineering
26. Licensed font candidate (IBM Plex Mono, Fairfax HD, custom), or keep
    platform monospace?
27. Is a new `designsystem` Android Gradle module still desired (§17.1), or
    is `shared/client-core/ui` enough?
28. Visual regression: screenshot tests on PC, or manual playtest only for now?

---

## 10. First implementation slice

Shipped with ADR-0021:

- Shared CRT palette + region accents + denser type tokens
- `TuiPanel`, `CrtFrame`, `TypewriterText`, `BlockCursor`, boot line model
- PC: CRT frame around app, boot sequence gate, ship map using `TuiPanel` +
  region accents + ALL-CAPS status lines
- Accessibility gates for CRT/motion; PC `largerText` density wiring
- Spec §15 + this document + accessibility audit note updated
