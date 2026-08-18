# ADR-0023: Bounded phosphor bloom on chrome

- Status: Accepted
- Date: 2026-08-18
- Deciders: Product request for a more visually stimulating client
- Amends: ADR-0022 decision 2 (CRT effects are scanlines plus vignette only)

## Context

ADR-0022 locked the CRT presentation to "scanlines plus vignette only" and
explicitly forbade text glow, geometric curvature, bloom, and global flicker.
That decision was made to protect readability for intimidated beginners, which
remains the right priority for long-form teaching content.

Subsequent visual-enrichment work, requested by the product owner, added a
three-pass phosphor halo to `drawTuiBorder` and a blur-22 text shadow to
`TuiPanel` titles. The result is what the product wanted and does not touch
body text — but it contradicts the accepted ADR. The docs and the code
currently give a contributor two different answers, and "no bloom" is being
enforced by nothing.

Rather than leave the contradiction standing, this ADR narrows the ban to
where it earns its keep and writes down an envelope the chrome must stay
inside.

## Decision

1. **Static bloom is permitted on chrome.** Panel borders, panel titles, status
   glyphs, meters, nav chrome, and region accents may carry a soft phosphor
   halo.
2. **Bloom is prohibited on content.** Briefing and lesson prose, code, SQL,
   terminal and test output, dialogue, and learner error messages render crisp,
   with no halo and no shadow. Readability wins wherever the player is reading
   to learn.
3. **The halo is bounded**, and the bounds live in code as
   `PhosphorBloom` constants rather than as prose here, so they are testable
   and every bloom site draws from one definition:
   - at most 2 halo passes beneath the crisp stroke
   - widest halo at most 7× the crisp stroke width
   - halo alpha at most 0.30, with the outer wash at 0.12
   - chrome title shadow blur at most 24px
4. **Bloom is static.** It never pulses, breathes, or flickers on its own. The
   ADR-0022 motion rules are unchanged: boot, typewriter, block cursor, and
   damaged-status blink remain the only intentional chrome motion, and they
   stay gated on `animatedChromeEnabled`.
5. **Curvature, barrel warp, and global idle flicker remain prohibited.** This
   ADR amends only the bloom and text-glow clause of ADR-0022 decision 2.
6. **Accessibility gates are unchanged.** High contrast and Disable CRT
   continue to force overlays off, and bloom rides the same chrome path, so a
   player who turns CRT off gets flat, crisp chrome.

## Alternatives considered

- **Back the glow out to match ADR-0022** — rejected. The enrichment is what
  the product asked for, it reads well, and it does not touch the text that
  readability rules were written to protect.
- **Amend ADR-0022 in place** — rejected. The repository's convention is to
  supersede or amend through a new ADR (ADR-0022 itself amends ADR-0021), which
  keeps the decision history readable.
- **Leave the contradiction and treat the ADR as aspirational** — rejected.
  An unenforced rule that the code already violates is worse than no rule: it
  makes every later "can I add an effect here?" ambiguous.
- **Permit animated glow (pulse/breathe on chrome)** — rejected for now. Idle
  motion was removed deliberately in ADR-0022 and would need its own
  seizure-safety review.

## Consequences

- `DESIGN_SYSTEM.md` §1 decision 3 and §6 record bloom as permitted-but-bounded
  on chrome.
- `PhosphorBloom` is the single source of the halo parameters; `drawTuiBorder`
  and `TuiPanel` read from it, and `PhosphorBloomTest` asserts the envelope.
- New chrome effects must state which side of the chrome/content line they sit
  on before they ship.
- The open question this raised in `GAME_FEEL.md` §5 is closed.

## References

- ADR-0021 Terminal-console visual language
- ADR-0022 Green phosphor terminal visual lock
- `docs/DESIGN_SYSTEM.md` §1, §5, §6, §7
- `docs/GAME_FEEL.md` §5
