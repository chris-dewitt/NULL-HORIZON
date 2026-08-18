# Game Feel: making NULL HORIZON fun and visually stimulating

**Status:** Working plan, 2026-08-18
**Scope:** Player-facing feel — reward, feedback, stakes, spectacle. Curriculum
correctness, mission engines, and content YAML are out of scope except where a
feel change reads state they already produce.
**Constraints:** Everything here stays inside the green-phosphor terminal lock
([ADR-0022](ADR/0022-green-phosphor-terminal-visual-lock.md)) and the
accessibility contract in [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) §7. No effect
ships without its reduced-motion, high-contrast, and disable-CRT path.

---

## 1. The honest diagnosis

The game is content-rich and mechanically deep: 77 missions, 80 skills, 14
region chapters, six working simulators (terminal, git, SQL, editor, service
map, pipeline, MLOps), a deterministic progression engine, and a genuinely good
story hook in the Auditor. What it lacks is not content. It lacks **payoff**.

Four specific gaps, all cheap to close relative to what is already built:

1. **The hub had no ship in it.** The ship map — the screen that carries the
   entire premise — was a wrapped row of text chips. Nothing looked damaged,
   nothing lit up, nothing changed shape as the player repaired the vessel.
2. **Rewards were unreadable.** 77 lore records are authored in
   `content/rewards/` with names and descriptions. The only place they surfaced
   was one debrief line printing raw ids: `Unlocked: lore.archive.missing_crew.01`.
   The player earns a thing they can never read.
3. **Nothing is at stake and nothing is measured.** There was no ship-wide
   condition readout anywhere in the app. Progress existed only as `3/6` inside
   whichever region you happened to tap.
4. **The mission loop has almost no moment-to-moment feedback.** Clearing an
   objective flips `[ ]` to `[x]` and changes a colour. Completing a mission is
   the only place with sound, haptics, and ceremony.

Items 1–3 are addressed by this change. Item 4 is the highest-value next step.

---

## 2. Shipped in this change

| Change | Where | Why it matters |
|---|---|---|
| **Hull schematic** replaces the chip grid | `ui/chrome/ShipSchematic.kt`, both clients | The hub now reads as a ship: nose cone, twin power spine with deck rungs, conduits out to every module, engine flare that brightens as regions come back online, and a charge pulse running aft once power flows. Modules stay real composables, so touch targets, focus order, and screen-reader labels are unchanged. |
| **Ship vitals strip** (HULL / PWR / DATA) | `progression/ShipVitals.kt`, `ShipVitalsStrip` | First thing on the hub. Pure projection of completed missions, fully restored regions, and decoded signals — it can never drift from progress. Block meters plus spelled-out percentages, so colour is never the only signal. |
| **Archive screen** | `ui/chrome/ArchiveLog.kt`, new nav destination on both clients | The 77 authored lore records are finally readable. Sealed records show a redacted title of the right shape, so an unrecovered record reads as a specific missing thing. |
| **Debrief names the unlock** | mission session VMs, both clients | `Unlocked: Missing Crew Ledger` instead of `lore.archive.missing_crew.01`. |
| **PC ship map reads real progress** | `pc/feature/shipmap/ShipMapViewModel.kt` | It was fourteen hardcoded placeholder statuses. Now it loads the same content bundle and progression store as Android. |
| **`TuiPanel` bottom-corner fix** | `ui/chrome/TuiPanel.kt` | The `└` `┘` glyphs were drawing on top of the last line of content in every panel in the app. |

---

## 3. Next — highest value per unit of work

### 3.1 Feedback on every objective, not just every mission

Right now a cleared objective is a colour change. This is the single biggest
fun gap, and it is the cheapest to close because the state already exists
(`MissionSessionState.isObjectiveComplete`).

- Flash the objective row on the transition to complete, strike the text, and
  play a short confirm tone — reuse `GameSound.Click`/`Success` and
  `HapticPulse`, both already wired on Android.
- Give failed commands a distinct error tone and a one-frame red border on the
  panel that rejected them, instead of silent stderr text.
- Type terminal output rather than dumping it. `TypewriterText` exists and is
  already gated on animated chrome; it is currently used only for dialogue.
- Announce the transition to screen readers as a polite live region — the
  existing `RankUpBanner` shows the pattern.

**Effort:** small. **Impact:** every mission, every objective, every session.

### 3.2 Restructure the mission session screen

`MissionSessionScreen.kt` is 1265 lines rendering one flat vertical scroll:
back button, title, summary, phase line, objectives, tool panel, narrative,
hint button, hints, action message, reset. On a phone the player scrolls past
the thing they are working on. Incident missions with three tools stack three
full workspaces.

- Pin mission title and a one-line objective counter to the top.
- Make the tool workspace the screen; move narrative, hints, and reset into a
  collapsible drawer.
- When a mission has more than one tool, use `TuiTabLine` between workspaces
  (already built, already the nav idiom) instead of stacking them.

**Effort:** medium. **Impact:** the screen players spend all their time on.

### 3.3 Make the ship react to failure

`ConsequenceDefinition` and `ConsequencePanel` exist (spec §9.9) but the world
never visibly worsens. Now that vitals and the schematic exist, wire them:

- A setback dips PWR on the strip and flips the affected module to a damaged
  blink for the rest of the session.
- Repairing it restores the conduit with a visible power-up — the schematic
  already draws the lit and unlit states, so this is a transition, not new art.

**Effort:** small once §3.1 lands. **Impact:** gives repairs weight.

### 3.4 Earnable cosmetics from rewards already in the schema

`reward.schema.json` carries a `kind`. Spec §13.3 lists terminal themes, drone
shells, sound packs, alternate map skins, clean-solution badges, incident
medals. None are implemented; `PalettePicker` currently offers every palette to
everyone from settings.

Start with the cheapest two:

- Gate two or three palettes behind rank, keeping a fully accessible default
  always available (a cosmetic unlock must never gate readability).
- "Clean solution" and "no-hint" badges — `bestAssistanceLevel` and
  `attemptCount` are already tracked per mission by `ProgressionEngine`.

**Effort:** small. **Impact:** gives clearance points somewhere to land.

---

## 4. Later — bigger swings

- **Daily systems (spec §13.4).** No streak, review incident, or rotating
  puzzle exists. The spec's rules are good ones (no shame notifications, an
  earned maintenance buffer); they just are not built.
- **Replayability (spec §13.5).** Mission YAML carries a `seed` that nothing
  varies on. Deterministic variation of filenames, values, and failure location
  would make review incidents worth replaying.
- **Skill map as a graph.** The prerequisite graph across 80 skills is
  validated acyclic at build time, and the screen renders it as a list. This is
  the second-best candidate for a drawn view after the ship map.
- **Audio.** Five WAVs and an ambient hum. Spec §15.4 asks for mechanical
  feedback, distinct success tones, and non-exhausting warnings. Key-clicks on
  terminal input and per-region ambient beds are the obvious wins.
- **An Auditor intrusion beat.** The signal fragments are the best writing in
  the project and they arrive as a quiet list entry. Decoding one deserves a
  red-shift takeover of the chrome for a few seconds, skippable and gated on
  animated chrome.

---

## 5. Decisions this raises

1. **The CRT lock has already drifted.** ADR-0022 §2 says "do not add text
   glow, geometric screen curvature, bloom, or global flicker", but
   `drawTuiBorder` now paints a three-pass phosphor halo and `TuiPanel` draws a
   22px text shadow on titles. The code is prettier than the ADR allows. Either
   amend ADR-0022 to permit bounded static bloom on chrome (recommended — it is
   what the product asked for and it does not touch body text), or back the
   glow out. Leaving them contradictory means the next contributor gets
   different answers from the docs and the code.
2. **Region accents in high-contrast mode.** `NhRegionAccent` keeps its
   semantic hues even under high contrast, so the schematic stays coloured
   there. Status text and glyphs carry the meaning either way, but this should
   be a stated decision rather than an accident of the token layering.
3. **Schematic height on phones.** The 14-region hull is roughly 620dp tall and
   scrolls on a phone. A denser two-per-row variant is possible; the current
   version favours full region names over fitting one screen.

---

## 6. Verification notes

The shared client-core UI is compiled by both clients' toolchains
(ADR-0020), and `pc-app` renders headlessly under Xvfb, so Compose surfaces can
be rendered offscreen with `ImageComposeScene` and inspected as PNGs without a
device. That is a cheap path to the visual regression screenshot tests listed
as an open question in DESIGN_SYSTEM.md §9 — the schematic in this change was
checked that way in early, late, and high-contrast states.
