# Performance profiling baseline

- Date: 2026-07-11
- Method: debug assemble + manual cold-start notes on emulator; unit suite timing

## Baselines (dev)

- `./scripts/check.sh` Android unit tests + `assembleDebug`: ~30–60s on CI-like VM
- Content validation (~400 documents): <2s
- Mission simulators are in-process and must remain offline-capable

## Follow-ups before production

- [ ] Macrobenchmark cold start on reference devices
- [ ] Frame timing on terminal/editor screens with large histories
- [ ] Bundle load time for full curriculum assets
- [ ] Memory footprint of SQL + editor sessions

No production blocker identified for closed testing.
