# ADR-0006: Simulated Git instead of native Git library in version 1

- Status: Accepted
- Date: 2026-07-11
- Deciders: Product specification baseline / Epic 4

## Context

Git missions need accurate state transitions (working tree, index, commits, branches, merges) on Android without embedding native Git.

## Decision

Implement a purpose-built deterministic Git repository simulator. Epic 4 supports `status`, `diff`, `add`, `commit`, `log`, `branch`, `switch`, and `merge`, including conflicted-path resolution via `--ours` / `--theirs`. Commit hashes are deterministic short hashes derived from commit content. The simulator never shells out to real Git and never touches the device filesystem.

## Alternatives considered

- Embed JGit / libgit2 — deferred; heavier, harder to constrain for pedagogy, and less predictable for mission assertions.
- Remote Git service — rejected for offline Version Vault missions.

## Consequences

- Mission environments declare an initial commit graph and working-tree state in content data.
- Objective assertions use `git_state` against simulator state.
- Later epics can extend commands (rebase, stash) behind the same model.

## References

- `docs/PRODUCT_SPEC.md` §9.3, §20.3, §32 Epic 4, §36 Task 6
