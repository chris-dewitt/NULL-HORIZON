# Vertical-slice playtest findings

- Date: 2026-07-11
- Build: content bundle `0.3.0` / Epic 11 branch
- Tester: agent dry-run against mission engines and content validation

## Path exercised

1. `emergency.wake_sequence.01` — systems panel wake actions complete in order.
2. `emergency.fault_log.01` — terminal navigation to fault log (existing).
3. `emergency.rogue_process.01` — `ps` then `kill 204`; life-support process remains.
4. `version.inspect_status.01` — `git status` + `git diff` only.
5. `version.recover_commit.01` — `git log` then `git checkout c1`.
6. `archive.missing_crew.01` — SQL missing-crew query (existing).
7. `automation.pressure_threshold.01` — threshold edit + tests (existing).
8. `automation.regression_guard.01` — online mission; offline fallback dialogue present; local fake fixtures evaluate.
9. `comms.api_trace.01` — service-map inspect → patch path.
10. `archive.unsafe_query.01` — placeholder query edit + tests.
11. `pipeline.telemetry_drop.01` — pipeline repair (existing).
12. `incident.ghost_registry.01` — terminal alert + SQL ghost row + git restore (three tools).

## Findings

- Process table and `kill` are clear once `ps` output is shown; hint level 4 naming the PID is appropriate for introductory difficulty.
- Commit checkout by short id (`c1`) is easier on mobile than full hashes; keep authored ids in recovery missions.
- Online mission offline fallback should remain visible during briefing/in-progress so airplane-mode players understand local fixtures.
- Integrated incident ordering (terminal → SQL → git) matches the note file and reduces tool thrash.
- No content-standard blockers found in the dry-run; human device playtest still recommended before store tracks.
