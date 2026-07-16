# NULL HORIZON Design System

**Status:** Active redesign baseline (ADR-0021, amended by ADR-0022)
**Design canvas:** PC Compose Desktop first; Android distilled
**North star:** Dense green phosphor operator terminal - **tmux x Palantir x
Nostromo** mood, without kitsch or glow-heavy cyberpunk

This document is the living visual contract for tokens, chrome, motion, and
accessibility. Implementation primitives live under
`shared/client-core/.../ui/`.

---

## 1. Locked product decisions (2026-07-14)

| # | Topic | Decision |
|---|---|---|
| 1 | Identity | **Modern TUI / tmux terminal** is the primary look: box-drawing panes, indexed tabs, status glyphs, dense operator copy. |
| 2 | Region accents | §3.2 table **confirmed** |
| 3 | CRT | **Scanlines + vignette only**. No glow, no curvature, no bloom, no global flicker. |
| 4 | Boot | Every cold start; click/key skips (**current**) |
| 5 | ALL-CAPS | **System chrome/status voice** is ALL-CAPS and dense. Long-form teaching/body text stays readable. |
| 6 | Android CRT | **Lean CRT on phones** (lighter scanlines/vignette). Same tokens/TUI; stronger static overlay on PC. See §6.1. |
| 7 | Disable CRT | **Separate accessibility pref** (`disableCrt`). Independent of reduced motion. High contrast still forces CRT off for readability. |
| 8 | Font | **Terminal** — VT323 (DEC VT320 console glyphs, SIL OFL). See `shared/client-core/.../fonts/`. |

---

## 2. Visual principles

1. **Sick terminal, not dashboard.** First viewport should feel like a dense
   ops console (tmux panes, Fallout terminal, Palantir-style status), not a
   Material gallery or Alien cosplay kit.
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
   stays touch-first with lean CRT.

---

## 3. Palette tokens

### 3.1 Base CRT

| Token | Role | Default |
|---|---|---|
| `CrtBlack` | App background | `#000703` |
| `CrtRaised` | Nested panel fill | `#03180D` |
| `CrtPanel` | Panel fill | `#011009` |
| `PhosphorWhite` | Primary readable text | `#DFFFE3` |
| `PhosphorGreen` | Nominal / OK / system | `#35FF6B` |
| `PhosphorAmber` | Warning / emergency accent | `#FFB000` |
| `PhosphorRed` | Critical / Black Vault shift | `#FF3344` |
| `PhosphorBlue` | Info / cold systems | `#44AAFF` |
| `PhosphorDim` | Secondary labels | `#6FA67A` |
| `Scanline` | Overlay stroke | white @ profile alpha |

High-contrast mode: pure black background, near-white foreground, accent kept
for focus only; CRT overlays forced off.

### 3.2 Region accents (confirmed)

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

---

## 4. Typography and density

- Font: **Terminal** face — VT323 (`NhTerminal-Regular.ttf`, SIL OFL). Fallback:
  platform monospace if the file fails to load.
- **System chrome** (nav, panel titles, region/status lines): **ALL-CAPS**.
- **Dialogue / narrative / learner errors:** sentence case.
- Larger-text: ~1.15× `fontScale` without breaking TUI geometry.

---

## 5. Components

| Primitive | Purpose | A11y notes |
|---|---|---|
| `TuiPanel` | Box-drawing bordered section | Title is text; not color-only |
| `CrtFrame` | Scanlines + vignette | Off if `disableCrt` or high contrast |
| `TypewriterText` | ORION/MICA dialogue reveal | Instant if animated chrome is disabled |
| `BlockCursor` | Blinking input caret | Static block if animated chrome is disabled |
| `BootSequence` | Launch OS check theatre | Every cold start; skippable; instant if animated chrome is disabled |
| `TuiRegionChip` | ALL-CAPS region + glyph status | Damaged blink respects animated chrome |

---

## 6. CRT profiles

| Profile | Where | Scanlines | Vignette | Curvature / bloom / flicker |
|---|---|---|---|---|
| `Medium` | PC default | Medium density/alpha | Stronger edge falloff | Off |
| `Lean` | Android phones | Lighter | Softer edge falloff | Off |

### 6.1 What “Android CRT” meant

Not “does Android get the redesign?” — tokens and TUI chrome ship to both.
The question was whether phones should also draw the stronger static CRT overlay,
which can hurt readability and battery on small screens. **Decision:** phones use
**Lean** CRT by default; PC uses **Medium**.
`disableCrt` turns overlays off on every platform.

---

## 7. Accessibility contract

| Pref | Effect |
|---|---|
| High contrast | White-on-black; CRT overlays and animated chrome forced off |
| Reduced motion | No boot animation, typewriter, cursor blink, or status blink (static CRT may remain) |
| Disable CRT | No scanlines or vignette |
| Larger text | `fontScale` boost; panels reflow; no clipped primary CTAs |
| Color-blind / color-only | Status always includes text or symbol, not hue alone |

---

## 8. Migration plan

1. Docs + tokens + shared primitives — done
2. PC shell: boot, CRT Medium, ship map `TuiPanel` — done
3. Disable CRT pref + Medium/Lean scanline-vignette CRT profiles - done
4. Terminal typeface (VT323) — done
5. PC mission session / list / skills / settings / profile — done
6. Android ship map lean TUI — done
7. Android missions / skills / settings / profile / mission session — done (this slice)
8. PC editor / service-map / pipeline / mlops TUI panels — done (this slice)
9. Residual polish: Material buttons/text fields inside onboarding/settings/mission surfaces - done
10. Optional later: Android bottom nav TUI density tuning

Do not rewrite mission engines or content YAML for cosmetics.

---

## 9. Remaining open questions

### Still open
- Global OK green vs region accent for success (default: global OK green)
- Black Vault red exact hue
- Damaged-status blink cadence and seizure-safety caps
- Replace PC `NavigationRail` with pure TUI column? **Done** (`TuiNavColumn`)
- Mission panel migration pace
- Keybind hints always on vs toggle
- Density target (% tighter)
- Typewriter cps / punctuation pauses
- Seizure-safety Hz caps beyond reduced motion
- Store screenshots CRT on/off
- `designsystem` Gradle module vs `shared/client-core/ui`
- Visual regression screenshot tests

### Answered (see §1)
Identity, region accents, CRT medium+real, boot every cold start, ALL-CAPS
chrome only, Android lean CRT, separate Disable CRT pref, **Terminal font (VT323)**.
