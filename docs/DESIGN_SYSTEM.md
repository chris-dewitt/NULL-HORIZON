# NULL HORIZON Design System

**Status:** Active redesign baseline (ADR-0021)  
**Design canvas:** PC Compose Desktop first; Android back-port  
**North star:** Dense operator terminal — **tmux × Palantir × Fallout** (MU-TH-UR is
inspirational tone only, not a costume to copy)

This document is the living visual contract for tokens, chrome, motion, and
accessibility. Implementation primitives live under
`shared/client-core/.../ui/`.

---

## 1. Locked product decisions (2026-07-13)

| # | Topic | Decision |
|---|---|---|
| 1 | Identity | **tmux / Palantir / Fallout terminal** is the primary look. MU-TH-UR is a north-star mood (cold ship OS), not a mandatory homage. |
| 2 | Region accents | §3.2 table **confirmed** |
| 3 | CRT | **Medium** intensity + **real geometric curvature** (not vignette-only) |
| 4 | Boot | Every cold start; click/key skips (**current**) |
| 5 | ALL-CAPS | **System chrome only** (nav, panel titles, status lines). ORION/MICA dialogue stays sentence case. |
| 6 | Android CRT | **Lean CRT on phones** (lighter scanlines, milder curve, no idle flicker). Same tokens/TUI; full medium CRT on PC. See §6.1. |
| 7 | Disable CRT | **Separate accessibility pref** (`disableCrt`). Independent of reduced motion. High contrast still forces CRT off for readability. |
| 8 | Font | **Still open** — keep `FontFamily.Monospace` until a licensed face is chosen |

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
| `CrtBlack` | App background | `#000000` |
| `CrtRaised` | Nested panel fill | `#0A0A0A` |
| `PhosphorWhite` | Primary readable text | `#E6E6E6` |
| `PhosphorGreen` | Nominal / OK / system | `#33FF66` |
| `PhosphorAmber` | Warning / emergency accent | `#FFB000` |
| `PhosphorRed` | Critical / Black Vault shift | `#FF3344` |
| `PhosphorBlue` | Info / cold systems | `#44AAFF` |
| `PhosphorDim` | Secondary labels | `#7A7A7A` |
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

- Font: platform monospace until a licensed CRT/terminal face is chosen (§1 #8).
- **System chrome** (nav, panel titles, region/status lines): **ALL-CAPS**.
- **Dialogue / narrative / learner errors:** sentence case.
- Larger-text: ~1.15× `fontScale` without breaking TUI geometry.

---

## 5. Components

| Primitive | Purpose | A11y notes |
|---|---|---|
| `TuiPanel` | Box-drawing bordered section | Title is text; not color-only |
| `CrtFrame` | Curvature + scanlines + vignette + bloom | Off if `disableCrt` or high contrast |
| `TypewriterText` | ORION/MICA dialogue reveal | Instant if reduced motion |
| `BlockCursor` | Blinking input caret | Static block if reduced motion |
| `BootSequence` | Launch OS check theatre | Every cold start; skippable; instant if reduced motion |
| `RegionStatusLine` | ALL-CAPS region + status | Accent + textual status |

---

## 6. CRT profiles

| Profile | Where | Scanlines | Curvature | Flicker | Bloom |
|---|---|---|---|---|---|
| `Medium` | PC default | Medium density/alpha | Real barrel-style warp + bezel | Rare idle | Soft phosphor edge |
| `Lean` | Android phones | Lighter | Milder warp | Off | Minimal |

### 6.1 What “Android CRT” meant

Not “does Android get the redesign?” — tokens and TUI chrome ship to both.
The question was whether phones should also draw the **full CRT overlay**
(scanlines / curve / flicker), which can hurt readability and battery on small
screens. **Decision:** phones use **Lean** CRT by default; PC uses **Medium**.
`disableCrt` turns overlays off on every platform.

---

## 7. Accessibility contract

| Pref | Effect |
|---|---|
| High contrast | White-on-black; CRT forced off |
| Reduced motion | No boot animation, typewriter, cursor blink, or flicker (static CRT may remain) |
| Disable CRT | No scanlines, curvature, vignette, bloom, or flicker |
| Larger text | `fontScale` boost; panels reflow; no clipped primary CTAs |
| Color-blind / color-only | Status always includes text or symbol, not hue alone |

---

## 8. Migration plan

1. Docs + tokens + shared primitives
2. PC shell: boot, CRT Medium, ship map `TuiPanel`
3. Disable CRT pref + Medium real curvature + Lean Android CRT
4. PC mission session / dialogue / terminal cursor
5. Remaining screens without Material cards

---

## 9. Remaining open questions

### Still open
- Font / licensed face (§1 #8)
- Global OK green vs region accent for success (default: global OK green)
- Black Vault red exact hue
- Flicker: rare idle OK, or boot-only?
- Replace PC `NavigationRail` with pure TUI column?
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
chrome only, Android lean CRT, separate Disable CRT pref.
