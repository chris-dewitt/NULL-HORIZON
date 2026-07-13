# Terminal typeface (locked)

**Decision:** UI typeface is **Terminal** — classic CRT/console glyphs.

**Implementation face:** [VT323](https://fonts.google.com/specimen/VT323)
(`NhTerminal-Regular.ttf`), designed from DEC VT320 terminal glyphs.
Licensed under the SIL Open Font License 1.1 — see `OFL.txt`.

Platforms load this file into `NullHorizonTheme(fontFamily = …)`:
- PC: classpath `fonts/NhTerminal-Regular.ttf`
- Android: `res/font/nh_terminal_regular.ttf`
